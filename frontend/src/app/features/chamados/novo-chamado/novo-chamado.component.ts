import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-novo-chamado',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, SidebarComponent],
  templateUrl: './novo-chamado.component.html',
  styleUrl: './novo-chamado.component.scss'
})
export class NovoChamadoComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  erro = '';
  clientes: Usuario[] = [];
  tecnicos: Usuario[] = [];

  isAdmin = false;
  isTecnico = false;
  isCliente = false;

  prioridades = [
    { label: 'Baixa', value: 0 },
    { label: 'Média', value: 1 },
    { label: 'Alta',  value: 2 }
  ];

  constructor(
    private fb: FormBuilder,
    private chamadoService: ChamadoService,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isAdmin   = this.authService.isAdmin();
    this.isTecnico = this.authService.isTecnico();
    this.isCliente = this.authService.isCliente();

    this.form = this.fb.group({
      titulo:     ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      observacoes:['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
      prioridade: [1, Validators.required],
      clienteId:  [null],
      tecnicoId:  [null]
    });

    if (this.isAdmin || this.isTecnico) {
      this.usuarioService.listar().subscribe({
        next: (usuarios) => {
          this.clientes = usuarios.filter(u => u.perfis.includes('ROLE_CLIENTE'));
          this.tecnicos = usuarios.filter(u => u.perfis.includes('ROLE_TECNICO'));
          if (this.isAdmin || this.isTecnico) {
            this.form.get('clienteId')?.setValidators(Validators.required);
            this.form.get('clienteId')?.updateValueAndValidity();
          }
        }
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.erro = '';

    const { titulo, observacoes, prioridade, clienteId, tecnicoId } = this.form.value;
    this.chamadoService.criar({
      titulo,
      observacoes,
      prioridade: Number(prioridade),
      clienteId: clienteId ? Number(clienteId) : undefined,
      tecnicoId: tecnicoId ? Number(tecnicoId) : undefined
    }).subscribe({
      next: () => this.router.navigate(['/chamados']),
      error: (err) => {
        this.erro = err?.error?.message || 'Erro ao criar chamado. Tente novamente.';
        this.loading = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/chamados']);
  }

  hasError(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && c.touched);
  }
}
