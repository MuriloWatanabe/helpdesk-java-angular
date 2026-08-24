import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensagemDoErro } from '../../../core/services/toast.service';
import { LogoComponent } from '../../../shared/logo/logo.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  loginForm: FormGroup;

  errorMessage = signal('');
  avisoSessao = signal('');
  isLoading = signal(false);
  mostrarSenha = false;

  private retorno = '/dashboard';

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;

    const retorno = params.get('retorno');
    if (retorno && !retorno.startsWith('/login')) {
      this.retorno = retorno;
    }
    if (params.get('expirado') === '1') {
      this.avisoSessao.set('Sua sessão expirou. Entre novamente para continuar.');
    } else if (params.get('emailAlterado') === '1') {
      this.avisoSessao.set('E-mail atualizado. Entre novamente usando o novo endereço.');
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.avisoSessao.set('');

    const { email, senha } = this.loginForm.value;

    this.authService.login(email, senha).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigateByUrl(this.retorno);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 0) {
          this.errorMessage.set(
            'Não foi possível conectar ao servidor. Verifique se a API está no ar.',
          );
        } else {
          this.errorMessage.set(mensagemDoErro(err, 'E-mail ou senha inválidos.'));
        }
      },
    });
  }

  usarDemo(email: string): void {
    this.loginForm.patchValue({ email, senha: '123456' });
  }

  get emailControl() {
    return this.loginForm.get('email');
  }

  get senhaControl() {
    return this.loginForm.get('senha');
  }
}
