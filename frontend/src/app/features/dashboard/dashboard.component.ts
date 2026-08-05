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
  @ViewChild('graficoStatus') graficoStatus?: ElementRef<HTMLCanvasElement>;
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

  /** Total ainda em aberto — o número que importa no dia a dia. */
  readonly emAberto = computed(() => {
    const s = this.stats();
    if (!s) return 0;
    return s.totalAbertos + s.totalEmAndamento + s.totalAguardandoCliente;
  });

  readonly classeStatus = classeStatus;
  readonly classePrioridade = classePrioridade;
  readonly textoSla = textoSla;

  private chartStatus: Chart | null = null;
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
        // Aguarda o próximo ciclo para os canvas existirem no DOM.
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

  // ------------------------------------------------------------------
  // Gráficos
  // ------------------------------------------------------------------

  private destruirGraficos(): void {
    this.chartStatus?.destroy();
    this.chartEvolucao?.destroy();
    this.chartStatus = null;
    this.chartEvolucao = null;
  }

  private desenharGraficos(): void {
    const dados = this.stats();
    if (!this.viewPronta || !dados) return;

    this.desenharStatus(dados);
    this.desenharEvolucao(dados);
  }

  /**
   * Distribuição por situação. O gráfico anterior misturava o total com as
   * partes na mesma escala, o que achatava as barras e induzia a erro.
   */
  private desenharStatus(dados: DashboardStats): void {
    const ctx = this.graficoStatus?.nativeElement.getContext('2d');
    if (!ctx || this.chartStatus) return;

    const fatias = [
      { rotulo: 'Abertos', valor: dados.totalAbertos, cor: '#2563eb' },
      { rotulo: 'Em andamento', valor: dados.totalEmAndamento, cor: '#d97706' },
      { rotulo: 'Aguardando cliente', valor: dados.totalAguardandoCliente, cor: '#7c3aed' },
      { rotulo: 'Resolvidos', valor: dados.totalResolvidos, cor: '#059669' },
      { rotulo: 'Encerrados', valor: dados.totalEncerrados, cor: '#64748b' },
      { rotulo: 'Cancelados', valor: dados.totalCancelados, cor: '#cbd5e1' },
    ].filter((f) => f.valor > 0);

    if (fatias.length === 0) return;

    this.chartStatus = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: fatias.map((f) => f.rotulo),
        datasets: [
          {
            data: fatias.map((f) => f.valor),
            backgroundColor: fatias.map((f) => f.cor),
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { boxWidth: 11, font: { size: 11.5 }, color: '#475569', padding: 12 },
          },
        },
      },
    });
  }

  /** Aberturas por dia nas últimas duas semanas. */
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

  // ------------------------------------------------------------------
  // Apresentação
  // ------------------------------------------------------------------

  /** Percentual de uma categoria sobre o total, para as barras de proporção. */
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
