import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { UsuarioService } from '../../core/services/usuario.service';
import { Usuario } from '../../core/models/usuario.model';

const PERFIS_MAP: Record<string, string> = {
  'ROLE_ADMIN':   'Admin',
  'ROLE_TECNICO': 'Técnico',
  'ROLE_CLIENTE': 'Cliente'
};

const PERFIS_OPTIONS = [
  { label: 'Admin',    value: 0 },
  { label: 'Cliente',  value: 1 },
  { label: 'Técnico',  value: 2 }
];

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, SidebarComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  loading = false;
  erro = '';

  showForm = false;
  formLoading = false;
  formErro = '';
  editingId: number | null = null;
  form!: FormGroup;

  showPerfisModal = false;
  perfisUsuario: Usuario | null = null;
  perfisForm!: FormGroup;
  perfisOptions = PERFIS_OPTIONS;

  constructor(
    private usuarioService: UsuarioService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarUsuarios();
    this.iniciarForm();
  }

  carregarUsuarios(): void {
    this.loading = true;
    this.usuarioService.listar().subscribe({
      next: (lista) => { this.usuarios = lista; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.erro = 'Erro ao carregar usuários.'; this.loading = false; this.cdr.detectChanges(); }
    });
  }

  iniciarForm(usuario?: Usuario): void {
    this.form = this.fb.group({
      nome:  [usuario?.nome ?? '',  [Validators.required, Validators.minLength(3)]],
      email: [usuario?.email ?? '', [Validators.required, Validators.email]],
      senha: ['', usuario ? [] : [Validators.required, Validators.minLength(6)]],
      perfis:[[[usuario ? this.perfisCodigos(usuario) : [1]]]]
    });
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
  }

  salvar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.formLoading = true;
    this.formErro = '';
    const { nome, email, senha, perfis } = this.form.value;

    const obs = this.editingId
      ? this.usuarioService.atualizar(this.editingId, { nome, email, senha: senha || undefined, perfis })
      : this.usuarioService.criar({ nome, email, senha, perfis });

    obs.subscribe({
      next: () => { this.fecharForm(); this.carregarUsuarios(); this.formLoading = false; this.cdr.detectChanges(); },
      error: (err) => {
        this.formErro = err?.error?.message || 'Erro ao salvar usuário.';
        this.formLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  excluir(id: number): void {
    if (!confirm('Tem certeza que deseja excluir este usuário?')) return;
    this.usuarioService.excluir(id).subscribe({
      next: () => this.carregarUsuarios(),
      error: () => alert('Erro ao excluir usuário.')
    });
  }

  abrirPerfis(usuario: Usuario): void {
    this.perfisUsuario = usuario;
    const codigos = this.perfisCodigos(usuario);
    this.perfisForm = this.fb.group({
      admin:   [codigos.includes(0)],
      cliente: [codigos.includes(1)],
      tecnico: [codigos.includes(2)]
    });
    this.showPerfisModal = true;
  }

  fecharPerfis(): void {
    this.showPerfisModal = false;
    this.perfisUsuario = null;
  }

  salvarPerfis(): void {
    if (!this.perfisUsuario) return;
    const { admin, cliente, tecnico } = this.perfisForm.value;
    const perfis: number[] = [];
    if (admin)   perfis.push(0);
    if (cliente) perfis.push(1);
    if (tecnico) perfis.push(2);
    if (perfis.length === 0) { alert('Selecione ao menos um perfil.'); return; }

    this.usuarioService.atualizarPerfis(this.perfisUsuario.id, perfis).subscribe({
      next: () => { this.fecharPerfis(); this.carregarUsuarios(); },
      error: () => alert('Erro ao atualizar perfis.')
    });
  }

  perfilLabel(role: string): string {
    return PERFIS_MAP[role] ?? role;
  }

  perfisCodigos(usuario: Usuario): number[] {
    return usuario.perfis.map(r => {
      if (r === 'ROLE_ADMIN')   return 0;
      if (r === 'ROLE_CLIENTE') return 1;
      if (r === 'ROLE_TECNICO') return 2;
      return -1;
    }).filter(c => c >= 0);
  }

  formatData(data: string): string {
    return data ? new Date(data).toLocaleDateString('pt-BR') : '—';
  }

  hasError(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && c.touched);
  }
}
