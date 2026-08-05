import { Component, inject, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { mensagemDoErro } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  loginForm: FormGroup;
  errorMessage = '';
  avisoSessao = '';
  isLoading = false;
  mostrarSenha = false;

  /** Destino original quando o usuário caiu no login por um guard. */
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
      this.avisoSessao = 'Sua sessão expirou. Entre novamente para continuar.';
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.avisoSessao = '';

    const { email, senha } = this.loginForm.value;

    this.authService.login(email, senha).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigateByUrl(this.retorno);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage =
            'Não foi possível conectar ao servidor. Verifique se a API está no ar.';
        } else {
          // O backend já devolve mensagens prontas (credenciais, usuário inativo).
          this.errorMessage = mensagemDoErro(err, 'E-mail ou senha inválidos.');
        }
      },
    });
  }

  /** Preenche as credenciais de demonstração para facilitar a avaliação. */
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
