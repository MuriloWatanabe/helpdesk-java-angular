import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { LogoComponent } from '../../shared/logo/logo.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, LogoComponent],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly confirmService = inject(ConfirmService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly aberta = signal(false);

  readonly usuario = this.authService.usuario;
  readonly isAdmin = this.authService.isAdmin;
  readonly isAtendente = this.authService.isAtendente;
  readonly isCliente = this.authService.isCliente;

  readonly userName = computed(() => {
    const u = this.usuario();
    return u?.nome || u?.email || 'Usuário';
  });

  readonly userRole = this.authService.perfilLabel;
  readonly userInitials = computed(() => this.authService.getIniciais(this.userName()));

  constructor() {
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.aberta.set(false));
  }

  alternarMenu(): void {
    this.aberta.update((v) => !v);
  }

  fecharMenu(): void {
    this.aberta.set(false);
  }

  async logout(): Promise<void> {
    const confirmado = await this.confirmService.perguntar({
      titulo: 'Sair do sistema',
      mensagem: 'Deseja encerrar a sessão agora?',
      confirmar: 'Sair',
    });
    if (!confirmado) return;

    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
