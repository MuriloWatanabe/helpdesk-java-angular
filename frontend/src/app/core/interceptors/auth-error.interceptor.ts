import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';


export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {

        if (!req.url.includes('/v1/auth/login')) {
          authService.logout();
          router.navigate(['/login'], {
            queryParams: { retorno: router.url, expirado: '1' },
          });
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
