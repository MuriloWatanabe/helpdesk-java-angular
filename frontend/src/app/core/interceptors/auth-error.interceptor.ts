import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Só encerra a sessão se a requisição foi autenticada e o servidor recusou o token.
        if (req.headers.has('Authorization')) {
          authService.encerrarSessao();
        }
      } else if (error.status === 403) {
        toast.erro(error.error?.message ?? 'Você não tem permissão para executar esta ação.');
      } else if (error.status === 0) {
        toast.erro('Não foi possível falar com o servidor. Verifique se a API está no ar.');
      }

      return throwError(() => error);
    }),
  );
};
