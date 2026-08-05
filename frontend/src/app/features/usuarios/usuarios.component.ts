import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { UsuarioService } from '../../core/services/usuario.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService, mensagemDoErro } from '../../core/services/toast.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { PerfilCodigo, Usuario } from '../../core/models/usuario.model';

const PERFIS_MAP: Record<string, string> = {
  ROLE_ADMIN: 'Admin',
  ROLE_TECNICO: 'Técnico',
  ROLE_CLIENTE: 'Cliente',
};

const PERFIS_OPTIONS = [
  { label: 'Administrador', value: PerfilCodigo.ADMIN, desc: 'Acesso total, incluindo gestão de usuários' },
  { label: 'Técnico', value: PerfilCodigo.TECNICO, desc: 'Atende chamados e vê a fila completa' },
  { label: 'Cliente', value: PerfilCodigo.CLIENTE, desc: 'Abre e acompanha os próprios chamados' },
];

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterModule, DatePipe, SidebarComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);
  private readonly fb = inject(FormBuilder);

  usuarios = signal<Usuario[]>([]);
  loading = signal(false);
  erro = signal('');

  // Filtros
  busca = '';
  filtroPerfil: number | null = null;
  filtroAtivo: boolean | null = null;

  // Formulário
  showForm = false;
  formLoading = false;
  formErro = '';
  editingId: number | null = null;
  form!: FormGroup;

  readonly perfisOptions = PERFIS_OPTIONS;
  readonly meuId = this.authService.getUsuarioAtual()?.id ?? null;

  ngOnInit(): void {
    this.iniciarForm();
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    this.loading.set(true);
    this.erro.set('');

    this.usuarioService
      .listar({
        q: this.busca.trim() || undefined,
        perfil: this.filtroPerfil ?? undefined,
        ativo: this.filtroAtivo ?? undefined,
      })
      .subscribe({
        next: (lista) => {
          this.usuarios.set(lista);
          this.loading.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar os usuários.');
          this.loading.set(false);
        },
      });
  }

  filtrar(): void {
    this.carregarUsuarios();
  }

  limparFiltros(): void {
    this.busca = '';
    this.filtroPerfil = null;
    this.filtroAtivo = null;
    this.carregarUsuarios();
  }

  // ------------------------------------------------------------------
  // Formulário
  // ------------------------------------------------------------------

  /**
   * Perfis são controles booleanos independentes.
   *
   * Antes o campo era montado como `perfis: [[[codigos]]]`, o que fazia o
   * FormControl guardar um array dentro de outro e enviar `perfis: [[1]]`
   * para a API — o cadastro de usuário pelo admin nunca gravava o perfil certo.
   */
  private iniciarForm(usuario?: Usuario): void {
    const codigos = usuario ? usuario.perfisCodigos : [PerfilCodigo.CLIENTE];

    this.form = this.fb.group({
      nome: [usuario?.nome ?? '', [Validators.required, Validators.minLength(3)]],
      email: [usuario?.email ?? '', [Validators.required, Validators.email]],
      senha: ['', usuario ? [Validators.minLength(6)] : [Validators.required, Validators.minLength(6)]],
      telefone: [usuario?.telefone ?? ''],
      cargo: [usuario?.cargo ?? ''],
      ativo: [usuario ? usuario.ativo : true],
      perfilAdmin: [codigos.includes(PerfilCodigo.ADMIN)],
      perfilTecnico: [codigos.includes(PerfilCodigo.TECNICO)],
      perfilCliente: [codigos.includes(PerfilCodigo.CLIENTE)],
    });
  }

  /** Converte os três checkboxes na lista de códigos esperada pela API. */
  private perfisSelecionados(): number[] {
    const v = this.form.value;
    const perfis: number[] = [];
    if (v.perfilAdmin) perfis.push(PerfilCodigo.ADMIN);
    if (v.perfilCliente) perfis.push(PerfilCodigo.CLIENTE);
    if (v.perfilTecnico) perfis.push(PerfilCodigo.TECNICO);
    return perfis;
  }

  abrirForm(usuario?: Usuario): void {
    this.editingId = usuario?.id ?? null;
    this.iniciarForm(usuario);
    this.formErro = '';
    this.showForm = true;
  }

  fecharForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.formErro = '';
  }

  salvar(): void {
    const perfis = this.perfisSelecionados();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (perfis.length === 0) {
      this.formErro = 'Selecione ao menos um perfil de acesso.';
      return;
    }

    this.formLoading = true;
    this.formErro = '';

    const { nome, email, senha, telefone, cargo, ativo } = this.form.value;
    const payload = {
      nome,
      email,
      senha: senha || undefined,
      telefone: telefone || null,
      cargo: cargo || null,
      ativo,
      perfis,
    };

    const requisicao = this.editingId
      ? this.usuarioService.atualizar(this.editingId, payload)
      : this.usuarioService.criar(payload);

    requisicao.subscribe({
      next: () => {
        this.toast.sucesso(this.editingId ? 'Usuário atualizado.' : 'Usuário criado.');
        this.formLoading = false;
        this.fecharForm();
        this.carregarUsuarios();
      },
      error: (err) => {
        this.formErro = mensagemDoErro(err, 'Não foi possível salvar o usuário.');
        this.formLoading = false;
      },
    });
  }

  // ------------------------------------------------------------------
  // Ações da lista
  // ------------------------------------------------------------------

  async alternarSituacao(usuario: Usuario): Promise<void> {
    const ativando = !usuario.ativo;

    const confirmado = await this.confirmService.perguntar({
      titulo: ativando ? 'Reativar usuário' : 'Desativar usuário',
      mensagem: ativando
        ? `${usuario.nome} voltará a acessar o sistema.`
        : `${usuario.nome} perderá o acesso, mas o histórico de chamados será mantido.`,
      confirmar: ativando ? 'Reativar' : 'Desativar',
      perigoso: !ativando,
    });
    if (!confirmado) return;

    this.usuarioService.alterarSituacao(usuario.id, ativando).subscribe({
      next: () => {
        this.toast.sucesso(ativando ? 'Usuário reativado.' : 'Usuário desativado.');
        this.carregarUsuarios();
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível alterar a situação.'),
    });
  }

  /**
   * Exclusão definitiva. Usuário com chamados vinculados é recusado pela API
   * com uma mensagem orientando a desativação — antes isso virava um 500.
   */
  async excluir(usuario: Usuario): Promise<void> {
    const confirmado = await this.confirmService.perguntar({
      titulo: `Excluir ${usuario.nome}?`,
      mensagem:
        'A exclusão é permanente e só funciona para usuários sem chamados. ' +
        'Para quem já tem histórico, prefira desativar a conta.',
      confirmar: 'Excluir',
      perigoso: true,
    });
    if (!confirmado) return;

    this.usuarioService.excluir(usuario.id).subscribe({
      next: () => {
        this.toast.sucesso('Usuário excluído.');
        this.carregarUsuarios();
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível excluir o usuário.'),
    });
  }

  // ------------------------------------------------------------------
  // Apresentação
  // ------------------------------------------------------------------

  perfilLabel(role: string): string {
    return PERFIS_MAP[role] ?? role;
  }

  classePerfil(role: string): string {
    if (role === 'ROLE_ADMIN') return 'perfil-admin';
    if (role === 'ROLE_TECNICO') return 'perfil-tecnico';
    return 'perfil-cliente';
  }

  hasError(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && c.touched);
  }

  get temFiltroAtivo(): boolean {
    return !!(this.busca || this.filtroPerfil !== null || this.filtroAtivo !== null);
  }
}
