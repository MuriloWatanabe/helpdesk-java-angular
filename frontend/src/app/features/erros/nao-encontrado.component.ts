import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/**
 * Rota inexistente. Antes o curinga `**` redirecionava para o login, o que
 * fazia um usuário logado achar que a sessão tinha caído por causa de um
 * simples erro de digitação na URL.
 */
@Component({
  selector: 'app-nao-encontrado',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="erro-page">
      <p class="erro-codigo">404</p>
      <h1 class="erro-titulo">Página não encontrada</h1>
      <p class="erro-msg">
        O endereço que você tentou abrir não existe ou foi movido.
      </p>
      <div class="erro-acoes">
        <button type="button" class="btn-secondary" (click)="voltar()">Voltar</button>
        <a class="btn-primary" [routerLink]="destinoPrincipal">
          {{ logado ? 'Ir para o painel' : 'Ir para o login' }}
        </a>
      </div>
    </div>
  `,
  styleUrl: './erros.scss',
})
export class NaoEncontradoComponent {
  private readonly location = inject(Location);
  private readonly auth = inject(AuthService);

  readonly logado = this.auth.isLoggedIn();
  readonly destinoPrincipal = this.logado ? '/dashboard' : '/login';

  voltar(): void {
    this.location.back();
  }
}
