import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

// Endpoints públicos: não enviam token, para um token antigo não interferir no login/cadastro.
const ROTAS_PUBLICAS = ['login', 'register', 'recuperar-senha', 'redefinir-senha'].map(
  (rota) => `${environment.apiUrl}/v1/auth/${rota}`,
);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (
    token &&
    req.url.startsWith(environment.apiUrl) &&
    !ROTAS_PUBLICAS.some((rota) => req.url.startsWith(rota))
  ) {
    const clonedReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`),
    });
    return next(clonedReq);
  }

  return next(req);
};
