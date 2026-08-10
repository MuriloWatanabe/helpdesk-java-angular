import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';


export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], { queryParams: { retorno: state.url } });
  }
  if (!auth.isAdmin()) {

    return router.createUrlTree(['/acesso-negado']);
  }
  return true;
};


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
