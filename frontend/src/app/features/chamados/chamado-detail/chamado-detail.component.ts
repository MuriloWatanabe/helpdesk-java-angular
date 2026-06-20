import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { Chamado } from '../../../core/models/chamado.model';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-chamado-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './chamado-detail.component.html',
  styleUrl: './chamado-detail.component.scss'
})
export class ChamadoDetailComponent implements OnInit {
  chamado: Chamado | null = null;
  loading = true;
  erro = '';
  alterandoStatus = false;
  linkCopiado = false;

  tecnicos: Usuario[] = [];
  tecnicoSelecionadoId: number | null = null;
  alterandoTecnico = false;

  readonly isAdmin: boolean;
  readonly isTecnico: boolean;

  readonly statusOpcoes = [
    { label: 'Aberto',       valor: 0, enum: 'ABERTO'       },
    { label: 'Em Andamento', valor: 1, enum: 'EM_ANDAMENTO'  },
    { label: 'Encerrado',    valor: 2, enum: 'ENCERRADO'     },
  ];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly chamadoService: ChamadoService,
    private readonly usuarioService: UsuarioService,
    private readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.isAdmin  = authService.isAdmin();
    this.isTecnico = authService.isTecnico();
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.chamadoService.buscarPorId(id).subscribe({
      next: (data: Chamado) => {
        this.chamado = data;
        this.tecnicoSelecionadoId = data.tecnico?.id ?? null;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erro = 'Chamado não encontrado.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });

    if (this.isAdmin) {
      this.usuarioService.listar().subscribe({
        next: (usuarios: Usuario[]) => {
          this.tecnicos = usuarios.filter(u => u.perfis.includes('ROLE_TECNICO'));
          this.cdr.detectChanges();
        }
      });
    }
  }

  get podeAlterarStatus(): boolean {
    return this.isAdmin || this.isTecnico;
  }

  alterarStatus(enumNome: string): void {
    if (!this.chamado || this.alterandoStatus) return;
    this.alterandoStatus = true;
    this.chamadoService.alterarStatus(this.chamado.id, enumNome).subscribe({
      next: (atualizado: Chamado) => {
        this.chamado = atualizado;
        this.alterandoStatus = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.alterandoStatus = false;
        this.cdr.detectChanges();
      }
    });
  }

  alterarTecnico(): void {
    if (!this.chamado || this.alterandoTecnico) return;
    this.alterandoTecnico = true;
    this.chamadoService.atualizar(this.chamado.id, {
      titulo: this.chamado.titulo,
      observacoes: this.chamado.observacoes,
      prioridade: this.chamado.prioridade,
      clienteId: this.chamado.cliente.id,
      tecnicoId: this.tecnicoSelecionadoId ?? undefined,
    }).subscribe({
      next: (atualizado: Chamado) => {
        this.chamado = atualizado;
        this.tecnicoSelecionadoId = atualizado.tecnico?.id ?? null;
        this.alterandoTecnico = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.alterandoTecnico = false;
        this.cdr.detectChanges();
      }
    });
  }

  copiarLink(): void {
    navigator.clipboard.writeText(globalThis.location.href).then(() => {
      this.linkCopiado = true;
      setTimeout(() => { this.linkCopiado = false; this.cdr.detectChanges(); }, 2000);
    });
  }

  voltar(): void {
    this.router.navigate(['/chamados']);
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

  formatData(data: string | null | undefined): string {
    if (!data) return '—';
    return new Date(data).toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
