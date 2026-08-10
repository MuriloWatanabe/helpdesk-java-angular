import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensagemDoErro } from '../../../core/services/toast.service';
import { LogoComponent } from '../../../shared/logo/logo.component';


@Component({
  selector: 'app-esqueci-senha',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent],
  templateUrl: './esqueci-senha.component.html',
  styleUrl: '../auth-card.scss',
})
export class EsqueciSenhaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });


  enviando = signal(false);
  mensagem = signal('');
  erro = signal('');
  linkGerado = signal('');

  get emailControl() {
    return this.form.get('email');
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.mensagem.set('');
    this.erro.set('');
    this.linkGerado.set('');

    this.authService.recuperarSenha(this.form.value.email).subscribe({
      next: (resposta) => {
        this.enviando.set(false);
        this.mensagem.set(resposta.mensagem);
        this.linkGerado.set(resposta.detalhe ?? '');
      },
      error: (err) => {
        this.enviando.set(false);
        this.erro.set(mensagemDoErro(err, 'Não foi possível processar a solicitação.'));
      },
    });
  }


  get caminhoDoLink(): string {
    try {
      const url = new URL(this.linkGerado());
      return url.pathname + url.search;
    } catch {
      return this.linkGerado();
    }
  }
}
