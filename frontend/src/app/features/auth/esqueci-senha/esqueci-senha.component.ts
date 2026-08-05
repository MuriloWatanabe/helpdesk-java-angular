import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensagemDoErro } from '../../../core/services/toast.service';

/**
 * "Esqueci minha senha" — a tela existia como link morto (href="#").
 *
 * O backend gera um token de uso único válido por 30 minutos. Como o projeto
 * não tem servidor de e-mail, em desenvolvimento o link volta na resposta e é
 * mostrado aqui; em produção basta desligar `app.reset-senha.expor-link`.
 */
@Component({
  selector: 'app-esqueci-senha',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './esqueci-senha.component.html',
  styleUrl: '../auth-card.scss',
})
export class EsqueciSenhaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  enviando = false;
  mensagem = '';
  erro = '';
  linkGerado = '';

  get emailControl() {
    return this.form.get('email');
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.mensagem = '';
    this.erro = '';
    this.linkGerado = '';

    this.authService.recuperarSenha(this.form.value.email).subscribe({
      next: (resposta) => {
        this.enviando = false;
        this.mensagem = resposta.mensagem;
        this.linkGerado = resposta.detalhe ?? '';
      },
      error: (err) => {
        this.enviando = false;
        this.erro = mensagemDoErro(err, 'Não foi possível processar a solicitação.');
      },
    });
  }

  /** Extrai o caminho relativo para navegar sem recarregar a aplicação. */
  get caminhoDoLink(): string {
    try {
      const url = new URL(this.linkGerado);
      return url.pathname + url.search;
    } catch {
      return this.linkGerado;
    }
  }
}
