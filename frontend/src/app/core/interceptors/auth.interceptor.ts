import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/**
 * Attaches the bearer token to every backend call, and clears the session when the backend
 * says the token is no longer good - an expired token would otherwise leave the app looking
 * signed in while every request failed.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  const authorized = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorized).pipe(
    catchError((error: HttpErrorResponse) => {
      const isLoginAttempt = request.url.endsWith('/login');
      if (error.status === 401 && !isLoginAttempt) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
