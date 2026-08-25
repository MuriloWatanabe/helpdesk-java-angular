import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService, mensagemDoErro } from '../../../core/services/toast.service';
import { Opcao, UsuarioDiretorio } from '../../../core/models/usuario.model';
import { PrioridadeChamado } from '../../../core/models/chamado.model';

@Component({
  selector: 'app-novo-chamado',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, SidebarComponent],
  templateUrl: './novo-chamado.component.html',
  styleUrl: './novo-chamado.component.scss',
})
export class NovoChamadoComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly chamadoService = inject(ChamadoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  form!: FormGroup;

  loading = signal(false);
  erro = signal('');

  clientes = signal<UsuarioDiretorio[]>([]);
  tecnicos = signal<UsuarioDiretorio[]>([]);
  prioridades = signal<Opcao[]>([]);
  categorias = signal<Opcao[]>([]);

  readonly isAtendente = this.authService.isAtendente;

  ngOnInit(): void {
    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      observacoes: [
        '',
        [Validators.required, Validators.minLength(10), Validators.maxLength(2000)],
      ],
      prioridade: [PrioridadeChamado.MEDIA, Validators.required],
      categoria: [null, Validators.required],
      clienteId: [null],
      tecnicoId: [null],
    });

    this.usuarioService.metadados().subscribe({
      next: (meta) => {
        this.prioridades.set(meta.prioridades);
        this.categorias.set(meta.categorias);
      },
    });

    if (this.isAtendente()) {
      this.form.get('clienteId')?.setValidators(Validators.required);
      this.form.get('clienteId')?.updateValueAndValidity();

      this.usuarioService.listarDiretorio().subscribe({
        next: (usuarios) => {
          this.clientes.set(usuarios.filter((u) => u.perfis.includes('ROLE_CLIENTE')));
          this.tecnicos.set(
            usuarios.filter(
              (u) => u.perfis.includes('ROLE_TECNICO') || u.perfis.includes('ROLE_ADMIN'),
            ),
          );
        },
      });
    }
  }

  get slaEscolhido(): string {
    const codigo = Number(this.form?.get('prioridade')?.value);
    const opcao = this.prioridades().find((p) => p.codigo === codigo);
    if (!opcao?.horasSla) return '';
    return opcao.horasSla >= 24
      ? `Prazo de atendimento: ${opcao.horasSla / 24} dia(s)`
      : `Prazo de atendimento: ${opcao.horasSla} horas`;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.erro.set('');

    const { titulo, observacoes, prioridade, categoria, clienteId, tecnicoId } = this.form.value;

    this.chamadoService
      .criar({
        titulo,
        observacoes,
        prioridade: Number(prioridade),
        categoria: Number(categoria),
        clienteId: clienteId ? Number(clienteId) : null,
        tecnicoId: tecnicoId ? Number(tecnicoId) : null,
      })
      .subscribe({
        next: (criado) => {
          this.toast.sucesso(`Chamado ${criado.numero} aberto com sucesso.`);

          this.router.navigate(['/chamados', criado.id]);
        },
        error: (err) => {
          this.erro.set(mensagemDoErro(err, 'Não foi possível abrir o chamado. Tente novamente.'));
          this.loading.set(false);
        },
      });
  }

  cancelar(): void {
    this.router.navigate(['/chamados']);
  }

  hasError(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && c.touched);
  }
}
