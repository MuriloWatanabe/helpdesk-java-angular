import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { Chamado } from '../../../core/models/chamado.model';

@Component({
  selector: 'app-chamados-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, SidebarComponent],
  templateUrl: './chamados-list.component.html',
  styleUrl: './chamados-list.component.scss'
})
export class ChamadosListComponent implements OnInit {
  chamados: Chamado[] = [];
  loading = false;
  error = '';

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;

  filtroStatus: number | null = null;
  busca = '';

  statusOptions = [
    { label: 'Todos', value: null },
    { label: 'Aberto', value: 0 },
    { label: 'Em Andamento', value: 1 },
    { label: 'Encerrado', value: 2 }
  ];

  constructor(
    private chamadoService: ChamadoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.chamadoService.listar(this.currentPage, this.pageSize).subscribe({
      next: (page) => {
        this.chamados = page.content;
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Erro ao carregar chamados. Verifique se o servidor está rodando.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filtrarStatus(status: number | null): void {
    this.filtroStatus = status;
    this.currentPage = 0;
    this.carregar();
  }

  get chamadosFiltrados(): Chamado[] {
    let lista = this.chamados;
    if (this.filtroStatus !== null) {
      lista = lista.filter(c => c.status === this.filtroStatus);
    }
    if (this.busca.trim()) {
      const b = this.busca.toLowerCase();
      lista = lista.filter(c =>
        c.titulo.toLowerCase().includes(b) ||
        c.cliente?.nome?.toLowerCase().includes(b)
      );
    }
    return lista;
  }

  paginaAnterior(): void {
    if (this.currentPage > 0) { this.currentPage--; this.carregar(); }
  }

  proximaPagina(): void {
    if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.carregar(); }
  }

  irParaPagina(p: number): void {
    this.currentPage = p;
    this.carregar();
  }

  get paginas(): number[] {
    const total = Math.min(this.totalPages, 7);
    return Array.from({ length: total }, (_, i) => i);
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
}
