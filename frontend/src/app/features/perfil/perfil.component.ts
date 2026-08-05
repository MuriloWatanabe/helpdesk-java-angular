import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService, mensagemDoErro } from '../../core/services/toast.service';
import { Usuario } from '../../core/models/usuario.model';

/**
 * Meus dados e troca de senha.
 *
 * Antes esta tela chamava PUT /v1/usuarios/{id} e POST de senha sem senha
 * atual — endpoints restritos ao administrador. Resultado: cliente e técnico
 * recebiam 403 ao salvar e o interceptor os deslogava. Agora usa os endpoints
 * de autosserviço (/v1/auth/me e /v1/auth/alterar-senha).
 */
@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule, RouterModule, DatePipe, NgClass, SidebarComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);

  usuario = signal<Usuario | null>(null);
  carregando = signal(true);

  // Dados pessoais
  editNome = '';
  editEmail = '';
  editTelefone = '';
  editCargo = '';
  salvandoDados = false;
  erroDados = '';

  // Troca de senha
  senhaAtual = '';
  novaSenha = '';
  confirmSenha = '';
  salvandoSenha = false;
  erroSenha = '';

  get iniciais(): string {
    return this.authService.getIniciais(this.usuario()?.nome);
  }

  get perfilLabel(): string {
    return this.usuario()?.perfilPrincipal ?? '—';
  }

  get perfilClasses(): string[] {
    const perfis = this.usuario()?.perfis ?? [];
    if (perfis.includes('ROLE_ADMIN')) return ['badge-admin'];
    if (perfis.includes('ROLE_TECNICO')) return ['badge-tecnico'];
    return ['badge-cliente'];
  }

  ngOnInit(): void {
    this.authService.carregarMeuPerfil().subscribe({
      next: (u) => {
        this.usuario.set(u);
        this.editNome = u.nome;
        this.editEmail = u.email;
        this.editTelefone = u.telefone ?? '';
        this.editCargo = u.cargo ?? '';
        this.carregando.set(false);
      },
      error: () => {
        this.toast.erro('Não foi possível carregar seus dados.');
        this.carregando.set(false);
      },
    });
  }

  salvarDados(): void {
    if (this.salvandoDados) return;
    this.erroDados = '';

    if (!this.editNome.trim() || !this.editEmail.trim()) {
      this.erroDados = 'Nome e e-mail são obrigatórios.';
      return;
    }
    if (this.editNome.trim().length < 3) {
      this.erroDados = 'O nome deve ter ao menos 3 caracteres.';
      return;
    }

    this.salvandoDados = true;
    this.authService
      .atualizarMeuPerfil({
        nome: this.editNome.trim(),
        email: this.editEmail.trim(),
        telefone: this.editTelefone.trim() || null,
        cargo: this.editCargo.trim() || null,
      })
      .subscribe({
        next: (atualizado) => {
          this.usuario.set(atualizado);
          this.salvandoDados = false;
          this.toast.sucesso('Dados atualizados com sucesso.');
        },
        error: (err) => {
          this.erroDados = mensagemDoErro(err, 'Não foi possível salvar. Tente novamente.');
          this.salvandoDados = false;
        },
      });
  }

  salvarSenha(): void {
    if (this.salvandoSenha) return;
    this.erroSenha = '';

    if (!this.senhaAtual) {
      this.erroSenha = 'Informe a senha atual.';
      return;
    }
    if (this.novaSenha.length < 6) {
      this.erroSenha = 'A nova senha deve ter no mínimo 6 caracteres.';
      return;
    }
    if (this.novaSenha !== this.confirmSenha) {
      this.erroSenha = 'As senhas não coincidem.';
      return;
    }

    this.salvandoSenha = true;
    this.authService
      .alterarSenha({ senhaAtual: this.senhaAtual, novaSenha: this.novaSenha })
      .subscribe({
        next: () => {
          this.senhaAtual = '';
          this.novaSenha = '';
          this.confirmSenha = '';
          this.salvandoSenha = false;
          this.toast.sucesso('Senha alterada com sucesso.');
        },
        error: (err) => {
          this.erroSenha = mensagemDoErro(err, 'Não foi possível alterar a senha.');
          this.salvandoSenha = false;
        },
      });
  }
}
