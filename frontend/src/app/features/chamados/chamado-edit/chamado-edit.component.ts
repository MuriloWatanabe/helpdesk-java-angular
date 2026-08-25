import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ToastService, mensagemDoErro } from '../../../core/services/toast.service';
import { Chamado } from '../../../core/models/chamado.model';
import { Opcao, UsuarioDiretorio } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-chamado-edit',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, SidebarComponent],
  templateUrl: './chamado-edit.component.html',
  styleUrl: '../novo-chamado/novo-chamado.component.scss',
})
export class ChamadoEditComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly chamadoService = inject(ChamadoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  form!: FormGroup;

  chamado = signal<Chamado | null>(null);
  carregando = signal(true);
  salvando = signal(false);
  erro = signal('');

  clientes = signal<UsuarioDiretorio[]>([]);
  tecnicos = signal<UsuarioDiretorio[]>([]);
  prioridades = signal<Opcao[]>([]);
  categorias = signal<Opcao[]>([]);

  private chamadoId = 0;

  ngOnInit(): void {
    this.chamadoId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      observacoes: [
        '',
        [Validators.required, Validators.minLength(10), Validators.maxLength(2000)],
      ],
      prioridade: [1, Validators.required],
      categoria: [null, Validators.required],
      clienteId: [null, Validators.required],
      tecnicoId: [null],
    });

    this.usuarioService.metadados().subscribe({
      next: (meta) => {
        this.prioridades.set(meta.prioridades);
        this.categorias.set(meta.categorias);
      },
    });

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

    if (!Number.isInteger(this.chamadoId) || this.chamadoId <= 0) {
      this.erro.set('Chamado inválido.');
      this.carregando.set(false);
      return;
    }

    this.carregar();
  }

  private carregar(): void {
    this.chamadoService.buscarPorId(this.chamadoId).subscribe({
      next: (dados) => {
        this.chamado.set(dados);
        this.form.patchValue({
          titulo: dados.titulo,
          observacoes: dados.observacoes,
          prioridade: dados.prioridade,
          categoria: dados.categoria,
          clienteId: dados.cliente?.id ?? null,
          tecnicoId: dados.tecnico?.id ?? null,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erro.set(mensagemDoErro(err, 'Não foi possível carregar o chamado.'));
        this.carregando.set(false);
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set('');

    const { titulo, observacoes, prioridade, categoria, clienteId, tecnicoId } = this.form.value;

    this.chamadoService
      .atualizar(this.chamadoId, {
        titulo,
        observacoes,
        prioridade: Number(prioridade),
        categoria: Number(categoria),
        clienteId: clienteId ? Number(clienteId) : null,
        tecnicoId: tecnicoId ? Number(tecnicoId) : null,
      })
      .subscribe({
        next: () => {
          this.toast.sucesso('Chamado atualizado.');
          this.router.navigate(['/chamados', this.chamadoId]);
        },
        error: (err) => {
          this.erro.set(mensagemDoErro(err, 'Não foi possível salvar as alterações.'));
          this.salvando.set(false);
        },
      });
  }

  cancelar(): void {
    this.router.navigate(['/chamados', this.chamadoId]);
  }

  hasError(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && c.touched);
  }

  get avisoPrazo(): string {
    const atual = this.chamado();
    const codigo = Number(this.form?.get('prioridade')?.value);
    if (!atual || codigo === atual.prioridade) return '';
    const opcao = this.prioridades().find((p) => p.codigo === codigo);
    if (!opcao?.horasSla) return '';
    return `O prazo será recalculado para ${opcao.horasSla}h a partir da abertura.`;
  }
}
