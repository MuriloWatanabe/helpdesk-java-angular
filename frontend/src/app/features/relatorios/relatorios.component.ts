import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js/auto';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { ToastService } from '../../core/services/toast.service';
import { DashboardStats } from '../../core/models/dashboard.model';

/**
 * Relatórios da operação: volume por prioridade e categoria, carga por técnico
 * e indicadores de SLA/satisfação. Exporta em CSV para uso fora do sistema.
 */
@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [RouterModule, DecimalPipe, SidebarComponent],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss',
})
export class RelatoriosComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('graficoPrioridade') graficoPrioridade?: ElementRef<HTMLCanvasElement>;
  @ViewChild('graficoCategoria') graficoCategoria?: ElementRef<HTMLCanvasElement>;

  private readonly dashboardService = inject(DashboardService);
  private readonly toast = inject(ToastService);

  stats = signal<DashboardStats | null>(null);
  loading = signal(true);
  erro = signal(false);

  private chartPrioridade: Chart | null = null;
  private chartCategoria: Chart | null = null;
  private viewPronta = false;

  ngOnInit(): void {
    this.carregar();
  }

  ngAfterViewInit(): void {
    this.viewPronta = true;
    this.desenhar();
  }

  ngOnDestroy(): void {
    this.destruir();
  }

  carregar(): void {
    this.loading.set(true);
    this.erro.set(false);
    this.destruir();

    this.dashboardService.getStats(true).subscribe({
      next: (dados) => {
        this.stats.set(dados);
        this.loading.set(false);
        setTimeout(() => this.desenhar(), 0);
      },
      error: () => {
        this.loading.set(false);
        this.erro.set(true);
      },
    });
  }

  // ------------------------------------------------------------------
  // Gráficos
  // ------------------------------------------------------------------

  private destruir(): void {
    this.chartPrioridade?.destroy();
    this.chartCategoria?.destroy();
    this.chartPrioridade = null;
    this.chartCategoria = null;
  }

  private desenhar(): void {
    const dados = this.stats();
    if (!this.viewPronta || !dados) return;

    const ctxPrioridade = this.graficoPrioridade?.nativeElement.getContext('2d');
    if (ctxPrioridade && !this.chartPrioridade && dados.porPrioridade.length > 0) {
      this.chartPrioridade = new Chart(ctxPrioridade, {
        type: 'bar',
        data: {
          labels: dados.porPrioridade.map((p) => p.rotulo),
          datasets: [
            {
              label: 'Chamados',
              data: dados.porPrioridade.map((p) => p.total),
              // Ordem BAIXA → URGENTE, seguindo a escala de urgência
              backgroundColor: ['#059669', '#d97706', '#dc2626', '#991b1b'],
              borderRadius: 5,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            x: { grid: { display: false }, ticks: { color: '#64748b', font: { size: 11.5 } } },
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(0,0,0,0.05)' },
              ticks: { color: '#64748b', font: { size: 11 }, stepSize: 1, precision: 0 },
            },
          },
        },
      });
    }

    const ctxCategoria = this.graficoCategoria?.nativeElement.getContext('2d');
    if (ctxCategoria && !this.chartCategoria && dados.porCategoria.length > 0) {
      this.chartCategoria = new Chart(ctxCategoria, {
        type: 'bar',
        data: {
          labels: dados.porCategoria.map((c) => c.rotulo),
          datasets: [
            {
              label: 'Chamados',
              data: dados.porCategoria.map((c) => c.total),
              backgroundColor: '#1d4ed8',
              borderRadius: 5,
            },
          ],
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            x: {
              beginAtZero: true,
              grid: { color: 'rgba(0,0,0,0.05)' },
              ticks: { color: '#64748b', font: { size: 11 }, stepSize: 1, precision: 0 },
            },
            y: { grid: { display: false }, ticks: { color: '#64748b', font: { size: 11.5 } } },
          },
        },
      });
    }
  }

  // ------------------------------------------------------------------
  // Exportação
  // ------------------------------------------------------------------

  /** Gera um CSV com os indicadores consolidados (separador ; para o Excel pt-BR). */
  exportarCsv(): void {
    const dados = this.stats();
    if (!dados) return;

    const linhas: string[][] = [
      ['Indicador', 'Valor'],
      ['Total de chamados', String(dados.totalChamados)],
      ['Abertos', String(dados.totalAbertos)],
      ['Em andamento', String(dados.totalEmAndamento)],
      ['Aguardando cliente', String(dados.totalAguardandoCliente)],
      ['Resolvidos', String(dados.totalResolvidos)],
      ['Encerrados', String(dados.totalEncerrados)],
      ['Cancelados', String(dados.totalCancelados)],
      ['Prazo estourado', String(dados.totalSlaVencido)],
      ['Vencendo em 8h', String(dados.totalSlaEmRisco)],
      ['Sem técnico', String(dados.totalSemTecnico)],
      ['Tempo médio de resolução (h)', String(dados.tempoMedioResolucaoHoras ?? '')],
      ['Nota média', String(dados.notaMediaAtendimento ?? '')],
      ['Total de avaliações', String(dados.totalAvaliacoes)],
      [],
      ['Prioridade', 'Chamados'],
      ...dados.porPrioridade.map((p) => [p.rotulo, String(p.total)]),
      [],
      ['Categoria', 'Chamados'],
      ...dados.porCategoria.map((c) => [c.rotulo, String(c.total)]),
      [],
      ['Técnico', 'Chamados atribuídos'],
      ...dados.porTecnico.map((t) => [t.rotulo, String(t.total)]),
    ];

    const csv = linhas.map((linha) => linha.join(';')).join('\n');
    // BOM para o Excel reconhecer os acentos como UTF-8
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `relatorio-helpdesk-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);

    this.toast.sucesso('Relatório exportado.');
  }

  imprimir(): void {
    globalThis.print();
  }

  get tempoMedioTexto(): string {
    const horas = this.stats()?.tempoMedioResolucaoHoras;
    if (horas === null || horas === undefined) return '—';
    return horas < 24 ? `${horas}h` : `${(horas / 24).toFixed(1)} dias`;
  }

  percentual(valor: number): number {
    const total = this.stats()?.totalChamados ?? 0;
    return total === 0 ? 0 : Math.round((valor / total) * 100);
  }
}
