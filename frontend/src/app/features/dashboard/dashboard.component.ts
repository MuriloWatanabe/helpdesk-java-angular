import { Component, AfterViewInit, OnInit, OnDestroy, ElementRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js/auto';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats } from '../../core/models/dashboard.model';
import { Chamado } from '../../core/models/chamado.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  userName = '';
  loading = true;
  erro = false;
  stats: DashboardStats | null = null;
  chamadosRecentes: Chamado[] = [];

  private _chart: Chart | null = null;
  private chartReady = false;
  private dataReady = false;

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {
    const user = this.authService.getUsuarioAtual();
    this.userName = user?.nome || user?.email || 'Usuário';
  }

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(forceRefresh = false): void {
    this.loading = true;
    this.erro = false;
    this._chart?.destroy();
    this._chart = null;
    this.dataReady = false;
    this.dashboardService.getStats(forceRefresh).subscribe({
      next: (data) => {
        this.stats = data;
        this.chamadosRecentes = data.chamadosRecentes;
        this.loading = false;
        this.dataReady = true;
        this.cdr.detectChanges();
        setTimeout(() => this.tryRenderChart(), 0);
      },
      error: () => {
        this.loading = false;
        this.erro = true;
        this.cdr.detectChanges();
      }
    });
  }

  ngAfterViewInit(): void {
    this.chartReady = true;
    this.tryRenderChart();
  }

  private tryRenderChart(): void {
    if (this.chartReady && this.dataReady && !this._chart) {
      this.createChart();
    }
  }

  getStatusLabel(s: number): string {
    return ({ 0: 'Aberto', 1: 'Em Andamento', 2: 'Encerrado' } as Record<number, string>)[s] ?? '—';
  }

  getStatusClass(s: number): string {
    return ({ 0: 'badge-aberto', 1: 'badge-andamento', 2: 'badge-encerrado' } as Record<number, string>)[s] ?? '';
  }

  getPrioridadeLabel(p: number): string {
    return ({ 0: 'Baixa', 1: 'Média', 2: 'Alta' } as Record<number, string>)[p] ?? '—';
  }

  getPrioridadeClass(p: number): string {
    return ({ 0: 'badge-baixa', 1: 'badge-media', 2: 'badge-alta' } as Record<number, string>)[p] ?? '';
  }

  formatData(data: string): string {
    if (!data) return '—';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  ngOnDestroy(): void {
    this._chart?.destroy();
  }

  private createChart(): void {
    if (!this.chartCanvas || !this.stats) return;
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const total = this.stats.totalChamados || 1;
    const abertos = this.stats.totalAbertos;
    const andamento = this.stats.totalEmAndamento;
    const encerrados = this.stats.totalEncerrados;

    this._chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Abertos', 'Em Andamento', 'Encerrados', 'Total'],
        datasets: [{
          label: 'Chamados',
          data: [abertos, andamento, encerrados, total],
          backgroundColor: [
            'rgba(29, 78, 216, 0.8)',
            'rgba(234, 88, 12, 0.8)',
            'rgba(5, 150, 105, 0.8)',
            'rgba(100, 116, 139, 0.8)'
          ],
          borderRadius: 4,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1e293b',
            titleColor: '#f8fafc',
            bodyColor: '#cbd5e1',
            padding: 10,
            cornerRadius: 8
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { font: { size: 12 }, color: '#64748b' }
          },
          y: {
            grid: { color: 'rgba(0,0,0,0.05)' },
            ticks: { font: { size: 12 }, color: '#64748b', stepSize: 1 },
            beginAtZero: true
          }
        }
      }
    });
  }
}
