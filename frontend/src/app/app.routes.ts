import { Routes } from '@angular/router';

import { adminGuard, authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'RouteWise - smart payment routing',
    loadComponent: () => import('./features/landing/landing').then((m) => m.Landing),
  },
  {
    path: 'login',
    title: 'Sign in - RouteWise',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'signup',
    title: 'Create account - RouteWise',
    loadComponent: () => import('./features/auth/signup/signup').then((m) => m.Signup),
  },
  {
    path: 'payments',
    title: 'Route a payment - RouteWise',
    canActivate: [authGuard],
    loadComponent: () => import('./features/payments/recommend/recommend').then((m) => m.Recommend),
  },
  {
    path: 'history',
    title: 'Transaction history - RouteWise',
    canActivate: [authGuard],
    loadComponent: () => import('./features/transactions/history/history').then((m) => m.History),
  },
  {
    path: 'admin/gateways',
    title: 'Gateway configuration - RouteWise',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/gateways/gateways').then((m) => m.Gateways),
  },
  // The landing page is the safe place to send a stray URL: it needs no session, so nobody
  // gets bounced through the sign-in screen just for mistyping a path.
  { path: '**', redirectTo: '' },
];
