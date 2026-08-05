import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function senhasIguais(group: AbstractControl): ValidationErrors | null {
  const senha = group.get('senha')?.value;
  const confirmar = group.get('confirmarSenha')?.value;
  return senha === confirmar ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  form: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6)]],
      confirmarSenha: ['', Validators.required],
      telefone: ['']
    }, { validators: senhasIguais });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { nome, email, senha, telefone } = this.form.value;

    this.authService.register(nome, email, senha, telefone || undefined).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Cadastro realizado! Redirecionando para o login...';
        setTimeout(() => this.router.navigate(['/login']), 1800);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 409 || err.error?.message?.includes('já cadastrado')) {
          this.errorMessage = 'Este e-mail já está cadastrado.';
        } else if (err.status === 0) {
          this.errorMessage = 'Não foi possível conectar ao servidor.';
        } else {
          this.errorMessage = err.error?.message || 'Ocorreu um erro. Tente novamente.';
        }
      }
    });
  }

  get nomeControl() { return this.form.get('nome'); }
  get emailControl() { return this.form.get('email'); }
  get senhaControl() { return this.form.get('senha'); }
  get confirmarSenhaControl() { return this.form.get('confirmarSenha'); }
  get telefoneControl() { return this.form.get('telefone'); }
}
