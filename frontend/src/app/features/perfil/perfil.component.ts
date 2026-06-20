import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuthService, UsuarioAtual } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss'
})
export class PerfilComponent implements OnInit {
  usuario: UsuarioAtual | null = null;
  dataCriacao: string | null = null;

  // Dados pessoais
  editNome = '';
  editEmail = '';
  salvandoDados = false;
  mensagemDados = '';
  erroDados = '';

  // Alterar senha
  novaSenha = '';
  confirmSenha = '';
  salvandoSenha = false;
  mensagemSenha = '';
  erroSenha = '';

  get iniciais(): string {
    return this.authService.getIniciais(this.usuario?.nome ?? '');
  }

  get perfilLabel(): string {
    return this.authService.getPerfilLabel(this.usuario?.perfis ?? []);
  }

  get perfilClasses(): string[] {
    const perfis = this.usuario?.perfis ?? [];
    if (perfis.includes('ROLE_ADMIN'))    return ['badge-admin'];
    if (perfis.includes('ROLE_TECNICO'))  return ['badge-tecnico'];
    return ['badge-cliente'];
  }

  constructor(
    private readonly authService: AuthService,
    private readonly usuarioService: UsuarioService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.usuario = this.authService.getUsuarioAtual();
    if (this.usuario) {
      this.editNome  = this.usuario.nome;
      this.editEmail = this.usuario.email;

      this.usuarioService.buscarPorId(this.usuario.id).subscribe({
        next: (u) => {
          this.dataCriacao = u.dataCriacao;
          this.cdr.detectChanges();
        }
      });
    }
  }

  salvarDados(): void {
    if (!this.usuario || this.salvandoDados) return;
    this.mensagemDados = '';
    this.erroDados = '';

    if (!this.editNome.trim() || !this.editEmail.trim()) {
      this.erroDados = 'Nome e e-mail são obrigatórios.';
      return;
    }

    this.salvandoDados = true;
    this.usuarioService.atualizar(this.usuario.id, {
      nome:  this.editNome.trim(),
      email: this.editEmail.trim(),
    }).subscribe({
      next: (atualizado) => {
        const usuarioNovo: UsuarioAtual = {
          id:     this.usuario!.id,
          nome:   atualizado.nome,
          email:  atualizado.email,
          perfis: this.usuario!.perfis,
        };
        localStorage.setItem('helpdesk_user', JSON.stringify(usuarioNovo));
        this.usuario       = usuarioNovo;
        this.mensagemDados = 'Dados atualizados com sucesso!';
        this.salvandoDados = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erroDados     = err?.error?.message ?? 'Erro ao salvar. Tente novamente.';
        this.salvandoDados = false;
        this.cdr.detectChanges();
      }
    });
  }

  salvarSenha(): void {
    if (!this.usuario || this.salvandoSenha) return;
    this.mensagemSenha = '';
    this.erroSenha     = '';

    if (!this.novaSenha || this.novaSenha.length < 6) {
      this.erroSenha = 'A senha deve ter no mínimo 6 caracteres.';
      return;
    }
    if (this.novaSenha !== this.confirmSenha) {
      this.erroSenha = 'As senhas não coincidem.';
      return;
    }

    this.salvandoSenha = true;
    this.usuarioService.atualizar(this.usuario.id, {
      nome:  this.usuario.nome,
      email: this.usuario.email,
      senha: this.novaSenha,
    }).subscribe({
      next: () => {
        this.novaSenha     = '';
        this.confirmSenha  = '';
        this.mensagemSenha = 'Senha alterada com sucesso!';
        this.salvandoSenha = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erroSenha     = err?.error?.message ?? 'Erro ao alterar senha.';
        this.salvandoSenha = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatData(data: string | null): string {
    if (!data) return '—';
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit', month: 'long', year: 'numeric'
    });
  }
}
