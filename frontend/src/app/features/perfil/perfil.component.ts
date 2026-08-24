import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormControl, FormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { ToastService, mensagemDoErro } from '../../core/services/toast.service';
import { Usuario } from '../../core/models/usuario.model';

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
  private readonly router = inject(Router);

  usuario = signal<Usuario | null>(null);
  carregando = signal(true);

  editNome = '';
  editEmail = '';
  editTelefone = '';
  editCargo = '';
  salvandoDados = signal(false);
  erroDados = signal('');

  senhaAtual = '';
  novaSenha = '';
  confirmSenha = '';
  salvandoSenha = signal(false);
  erroSenha = signal('');

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
    if (this.salvandoDados()) return;
    this.erroDados.set('');

    const nome = this.editNome.trim();
    const email = this.editEmail.trim().toLowerCase();
    const telefone = this.editTelefone.trim();
    const cargo = this.editCargo.trim();

    if (!nome || !email) {
      this.erroDados.set('Nome e e-mail são obrigatórios.');
      return;
    }
    if (nome.length < 3 || nome.length > 100) {
      this.erroDados.set('O nome deve ter entre 3 e 100 caracteres.');
      return;
    }
    if (Validators.email(new FormControl(email))) {
      this.erroDados.set('Informe um e-mail válido.');
      return;
    }
    if (telefone.length > 20) {
      this.erroDados.set('O telefone deve ter no máximo 20 caracteres.');
      return;
    }
    if (cargo.length > 100) {
      this.erroDados.set('O cargo deve ter no máximo 100 caracteres.');
      return;
    }

    const emailAnterior = this.usuario()?.email.trim().toLowerCase();
    this.salvandoDados.set(true);
    this.authService
      .atualizarMeuPerfil({
        nome,
        email,
        telefone: telefone || null,
        cargo: cargo || null,
      })
      .subscribe({
        next: (atualizado) => {
          this.usuario.set(atualizado);
          this.salvandoDados.set(false);
          if (atualizado.email.trim().toLowerCase() !== emailAnterior) {
            this.authService.logout();
            this.router.navigate(['/login'], { queryParams: { emailAlterado: '1' } });
            return;
          }

          this.editNome = atualizado.nome;
          this.editEmail = atualizado.email;
          this.editTelefone = atualizado.telefone ?? '';
          this.editCargo = atualizado.cargo ?? '';
          this.toast.sucesso('Dados atualizados com sucesso.');
        },
        error: (err) => {
          this.erroDados.set(mensagemDoErro(err, 'Não foi possível salvar. Tente novamente.'));
          this.salvandoDados.set(false);
        },
      });
  }

  salvarSenha(): void {
    if (this.salvandoSenha()) return;
    this.erroSenha.set('');

    if (!this.senhaAtual) {
      this.erroSenha.set('Informe a senha atual.');
      return;
    }
    if (this.novaSenha.length < 6) {
      this.erroSenha.set('A nova senha deve ter no mínimo 6 caracteres.');
      return;
    }
    if (this.novaSenha !== this.confirmSenha) {
      this.erroSenha.set('As senhas não coincidem.');
      return;
    }

    this.salvandoSenha.set(true);
    this.authService
      .alterarSenha({ senhaAtual: this.senhaAtual, novaSenha: this.novaSenha })
      .subscribe({
        next: () => {
          this.senhaAtual = '';
          this.novaSenha = '';
          this.confirmSenha = '';
          this.salvandoSenha.set(false);
          this.toast.sucesso('Senha alterada com sucesso.');
        },
        error: (err) => {
          this.erroSenha.set(mensagemDoErro(err, 'Não foi possível alterar a senha.'));
          this.salvandoSenha.set(false);
        },
      });
  }
}
