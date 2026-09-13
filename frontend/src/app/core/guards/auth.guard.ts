import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Keeps signed-out visitors on the login screen. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { redirectTo: state.url } });
};

/**
 * Admin-only routes. This is a convenience for the UI, not the security boundary - the
 * backend refuses non-admin calls to the gateway endpoints regardless of what the router did.
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAdmin()) {
    return true;
  }
  return router.createUrlTree(['/payments']);
};
