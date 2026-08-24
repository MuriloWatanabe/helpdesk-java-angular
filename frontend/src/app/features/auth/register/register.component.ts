import { Component, signal } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LogoComponent } from '../../../shared/logo/logo.component';

function senhasIguais(group: AbstractControl): ValidationErrors | null {
  const senha = group.get('senha')?.value;
  const confirmar = group.get('confirmarSenha')?.value;
  return senha === confirmar ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  form: FormGroup;

  isLoading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group(
      {
        nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
        email: ['', [Validators.required, Validators.email]],
        senha: ['', [Validators.required, Validators.minLength(6)]],
        confirmarSenha: ['', Validators.required],
        telefone: ['', Validators.maxLength(20)],
      },
      { validators: senhasIguais },
    );
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const { nome, email, senha, telefone } = this.form.value;

    this.authService.register(nome, email, senha, telefone || undefined).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Cadastro realizado! Redirecionando para o login...');
        setTimeout(() => this.router.navigate(['/login']), 1800);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409 || err.error?.message?.includes('já cadastrado')) {
          this.errorMessage.set('Este e-mail já está cadastrado.');
        } else if (err.status === 0) {
          this.errorMessage.set('Não foi possível conectar ao servidor.');
        } else {
          this.errorMessage.set(err.error?.message || 'Ocorreu um erro. Tente novamente.');
        }
      },
    });
  }

  get nomeControl() {
    return this.form.get('nome');
  }
  get emailControl() {
    return this.form.get('email');
  }
  get senhaControl() {
    return this.form.get('senha');
  }
  get confirmarSenhaControl() {
    return this.form.get('confirmarSenha');
  }
  get telefoneControl() {
    return this.form.get('telefone');
  }
}
