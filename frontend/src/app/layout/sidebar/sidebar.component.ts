import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly confirmService = inject(ConfirmService);
  private readonly router = inject(Router);

  /** Em telas estreitas a sidebar vira um painel deslizante. */
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
    // Navegar em um celular deve fechar o menu.
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
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
