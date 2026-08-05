import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Telas exclusivas do administrador (gestão de usuários). */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], { queryParams: { retorno: state.url } });
  }
  if (!auth.isAdmin()) {
    // Tela explicando o bloqueio, em vez de um redirecionamento silencioso.
    return router.createUrlTree(['/acesso-negado']);
  }
  return true;
};

/** Telas de operação (fila de atendimento, relatórios): admin e técnico. */
export const atendenteGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], { queryParams: { retorno: state.url } });
  }
  if (!auth.isAtendente()) {
    return router.createUrlTree(['/acesso-negado']);
  }
  return true;
};
