import { Component, inject, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService, mensagemDoErro } from '../../../core/services/toast.service';
import { LogoComponent } from '../../../shared/logo/logo.component';


function senhasIguais(grupo: AbstractControl): ValidationErrors | null {
  const nova = grupo.get('novaSenha')?.value;
  const confirmacao = grupo.get('confirmacao')?.value;
  return nova && confirmacao && nova !== confirmacao ? { senhasDiferentes: true } : null;
}

@Component({
  selector: 'app-redefinir-senha',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LogoComponent],
  templateUrl: './redefinir-senha.component.html',
  styleUrl: '../auth-card.scss',
})
export class RedefinirSenhaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  form: FormGroup = this.fb.group(
    {
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmacao: ['', [Validators.required]],
    },
    { validators: senhasIguais },
  );

  token = '';

  salvando = signal(false);
  erro = signal('');

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.erro.set('Link inválido. Solicite uma nova redefinição de senha.');
    }
  }

  get novaSenhaControl() {
    return this.form.get('novaSenha');
  }

  get confirmacaoControl() {
    return this.form.get('confirmacao');
  }

  salvar(): void {
    if (!this.token || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set('');

    this.authService.redefinirSenha(this.token, this.form.value.novaSenha).subscribe({
      next: () => {
        this.salvando.set(false);
        this.toast.sucesso('Senha redefinida. Faça login com a nova senha.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.salvando.set(false);
        this.erro.set(mensagemDoErro(err, 'Não foi possível redefinir a senha.'));
      },
    });
  }
}
