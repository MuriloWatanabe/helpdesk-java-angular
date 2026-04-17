import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js/auto';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';

interface StatCard {
  title: string;
  value: string | number;
  subtitle: string;
  colorClass: string;
  icon: string;
}

interface Categoria {
  nome: string;
  percentual: number;
  colorClass: string;
}

interface Chamado {
  id: string;
  titulo: string;
  solicitante: string;
  categoria: string;
  prioridade: 'Alta' | 'Média' | 'Baixa';
  status: 'Aberto' | 'Em Andamento' | 'Resolvido';
  data: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements AfterViewInit {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  statCards: StatCard[] = [
    {
      title: 'Total Abertos',
      value: 48,
      subtitle: '+5 desde ontem',
      colorClass: 'stat-blue',
      icon: 'folder'
    },
    {
      title: 'Em Andamento',
      value: 23,
      subtitle: '+2 desde ontem',
      colorClass: 'stat-orange',
      icon: 'clock'
    },
    {
      title: 'Resolvidos hoje',
      value: 11,
      subtitle: '+3 desde ontem',
      colorClass: 'stat-green',
      icon: 'check'
    },
    {
      title: 'Tempo médio',
      value: '4.2h',
      subtitle: '-0.3h desde ontem',
      colorClass: 'stat-purple',
      icon: 'timer'
    }
  ];

  categorias: Categoria[] = [
    { nome: 'Infraestrutura', percentual: 34, colorClass: 'bar-blue' },
    { nome: 'Software', percentual: 28, colorClass: 'bar-indigo' },
    { nome: 'Hardware', percentual: 21, colorClass: 'bar-orange' },
    { nome: 'Acesso/Senhas', percentual: 17, colorClass: 'bar-green' }
  ];

  chamadosRecentes: Chamado[] = [
    {
      id: '#1042',
      titulo: 'Impressora não responde',
      solicitante: 'Ana Silva',
      categoria: 'Hardware',
      prioridade: 'Alta',
      status: 'Aberto',
      data: '16/04/2026'
    },
    {
      id: '#1041',
      titulo: 'Sem acesso ao sistema ERP',
      solicitante: 'Carlos Souza',
      categoria: 'Software',
      prioridade: 'Alta',
      status: 'Em Andamento',
      data: '16/04/2026'
    },
    {
      id: '#1040',
      titulo: 'Redefinição de senha AD',
      solicitante: 'Mariana Costa',
      categoria: 'Acesso/Senhas',
      prioridade: 'Baixa',
      status: 'Resolvido',
      data: '15/04/2026'
    },
    {
      id: '#1039',
      titulo: 'Lentidão na rede Wi-Fi',
      solicitante: 'Pedro Lima',
      categoria: 'Infraestrutura',
      prioridade: 'Média',
      status: 'Em Andamento',
      data: '15/04/2026'
    },
    {
      id: '#1038',
      titulo: 'Monitor com tela piscando',
      solicitante: 'Julia Rocha',
      categoria: 'Hardware',
      prioridade: 'Média',
      status: 'Aberto',
      data: '14/04/2026'
    }
  ];

  private chart: Chart | null = null;

  ngAfterViewInit(): void {
    this.createChart();
  }

  private createChart(): void {
    if (!this.chartCanvas) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'],
        datasets: [
          {
            label: 'Abertos',
            data: [12, 19, 8, 15, 22, 7, 4],
            backgroundColor: 'rgba(29, 78, 216, 0.8)',
            borderRadius: 4,
            borderSkipped: false
          },
          {
            label: 'Resolvidos',
            data: [8, 14, 6, 11, 17, 5, 3],
            backgroundColor: 'rgba(5, 150, 105, 0.8)',
            borderRadius: 4,
            borderSkipped: false
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 16,
              font: { size: 12 },
              usePointStyle: true,
              pointStyleWidth: 10
            }
          },
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
            ticks: { font: { size: 12 }, color: '#64748b', stepSize: 5 },
            beginAtZero: true
          }
        }
      }
    });
  }
}
