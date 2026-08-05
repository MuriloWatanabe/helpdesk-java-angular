import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Exige sessão válida. `isLoggedIn()` também rejeita token expirado, então o
 * usuário volta ao login em vez de entrar numa tela que falharia em seguida.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Guarda o destino para retomar a navegação após o login.
  return router.createUrlTree(['/login'], { queryParams: { retorno: state.url } });
};
