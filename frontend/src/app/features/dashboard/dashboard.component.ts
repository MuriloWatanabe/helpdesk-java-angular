import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js/auto';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats } from '../../core/models/dashboard.model';
import { classePrioridade, classeStatus, textoSla } from '../chamados/chamado-ui';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule, DatePipe, DecimalPipe, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('graficoEvolucao') graficoEvolucao?: ElementRef<HTMLCanvasElement>;

  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);

  stats = signal<DashboardStats | null>(null);
  loading = signal(true);
  erro = signal(false);

  readonly isCliente = this.authService.isCliente;
  readonly isAtendente = this.authService.isAtendente;
  readonly usuario = this.authService.usuario;

  readonly primeiroNome = computed(() => {
    const nome = this.usuario()?.nome ?? '';
    return nome.split(' ')[0] || 'Bem-vindo';
  });


  readonly emAberto = computed(() => {
    const s = this.stats();
    if (!s) return 0;
    return s.totalAbertos + s.totalEmAndamento + s.totalAguardandoCliente;
  });


  readonly situacoes = computed(() => {
    const s = this.stats();
    if (!s) return [];

    const partes = [
      { rotulo: 'Abertos', valor: s.totalAbertos, cor: '#2563eb' },
      { rotulo: 'Em andamento', valor: s.totalEmAndamento, cor: '#d97706' },
      { rotulo: 'Aguardando cliente', valor: s.totalAguardandoCliente, cor: '#7c3aed' },
      { rotulo: 'Resolvidos', valor: s.totalResolvidos, cor: '#059669' },
      { rotulo: 'Encerrados', valor: s.totalEncerrados, cor: '#475569' },
      { rotulo: 'Cancelados', valor: s.totalCancelados, cor: '#94a3b8' },
    ].filter((p) => p.valor > 0);

    const total = partes.reduce((soma, p) => soma + p.valor, 0);
    return partes.map((p) => ({
      ...p,
      pct: total > 0 ? Math.round((p.valor / total) * 100) : 0,
    }));
  });


  readonly totalSituacoes = computed(() =>
    this.situacoes().reduce((soma, p) => soma + p.valor, 0),
  );


  readonly classeStatus = classeStatus;
  readonly classePrioridade = classePrioridade;
  readonly textoSla = textoSla;

  private chartEvolucao: Chart | null = null;
  private viewPronta = false;

  ngOnInit(): void {
    this.carregarDados();
  }

  ngAfterViewInit(): void {
    this.viewPronta = true;
    this.desenharGraficos();
  }

  ngOnDestroy(): void {
    this.destruirGraficos();
  }

  carregarDados(forceRefresh = false): void {
    this.loading.set(true);
    this.erro.set(false);
    this.destruirGraficos();

    this.dashboardService.getStats(forceRefresh).subscribe({
      next: (dados) => {
        this.stats.set(dados);
        this.loading.set(false);

        setTimeout(() => this.desenharGraficos(), 0);
      },
      error: () => {
        this.loading.set(false);
        this.erro.set(true);
      },
    });
  }

  atualizar(): void {
    this.carregarDados(true);
  }


  private destruirGraficos(): void {
    this.chartEvolucao?.destroy();
    this.chartEvolucao = null;
  }

  private desenharGraficos(): void {
    const dados = this.stats();
    if (!this.viewPronta || !dados) return;

    this.desenharEvolucao(dados);
  }


  private desenharEvolucao(dados: DashboardStats): void {
    const ctx = this.graficoEvolucao?.nativeElement.getContext('2d');
    if (!ctx || this.chartEvolucao || dados.aberturasPorDia.length === 0) return;

    this.chartEvolucao = new Chart(ctx, {
      type: 'line',
      data: {
        labels: dados.aberturasPorDia.map((p) => this.rotuloDia(p.data)),
        datasets: [
          {
            label: 'Chamados abertos',
            data: dados.aberturasPorDia.map((p) => p.total),
            borderColor: '#1d4ed8',
            backgroundColor: 'rgba(29, 78, 216, 0.12)',
            borderWidth: 2,
            fill: true,
            tension: 0.32,
            pointRadius: 2.5,
            pointBackgroundColor: '#1d4ed8',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false }, ticks: { font: { size: 10.5 }, color: '#64748b' } },
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' },
            ticks: { font: { size: 11 }, color: '#64748b', stepSize: 1, precision: 0 },
          },
        },
      },
    });
  }

  private rotuloDia(iso: string): string {
    const [, mes, dia] = iso.split('-');
    return `${dia}/${mes}`;
  }


  percentual(valor: number): number {
    const total = this.stats()?.totalChamados ?? 0;
    return total === 0 ? 0 : Math.round((valor / total) * 100);
  }

  get tempoMedioTexto(): string {
    const horas = this.stats()?.tempoMedioResolucaoHoras;
    if (horas === null || horas === undefined) return '—';
    if (horas < 24) return `${horas}h`;
    return `${(horas / 24).toFixed(1)} dias`;
  }
}
