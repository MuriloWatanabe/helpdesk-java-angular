import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/** Usuário autenticado tentando abrir uma tela fora do seu perfil. */
@Component({
  selector: 'app-acesso-negado',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="erro-page">
      <p class="erro-codigo">403</p>
      <h1 class="erro-titulo">Acesso restrito</h1>
      <p class="erro-msg">
        Esta área não está disponível para o perfil
        <strong>{{ perfil() }}</strong>. Se você precisa desse acesso,
        fale com um administrador do sistema.
      </p>
      <div class="erro-acoes">
        <button type="button" class="btn-secondary" (click)="voltar()">Voltar</button>
        <a class="btn-primary" routerLink="/dashboard">Ir para o painel</a>
      </div>
    </div>
  `,
  styleUrl: './erros.scss',
})
export class AcessoNegadoComponent {
  private readonly location = inject(Location);
  private readonly auth = inject(AuthService);

  readonly perfil = this.auth.perfilLabel;

  voltar(): void {
    this.location.back();
  }
}
