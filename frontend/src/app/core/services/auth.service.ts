import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../api.config';
import { LoginRequest, LoginResponse, Role, SignupRequest } from '../models/auth.model';

const TOKEN_KEY = 'fawry.token';
const USER_KEY = 'fawry.user';

interface StoredUser {
  id: number;
  email: string;
  role: Role;
}

/**
 * Holds the session: the JWT plus who it belongs to.
 *
 * <p>The token is kept in localStorage so a refresh does not log the user out, and mirrored
 * into a signal so the shell can react to sign-in and sign-out without polling.</p>
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly userSignal = signal<StoredUser | null>(readStoredUser());

  readonly currentUser = this.userSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.userSignal() !== null);
  readonly isAdmin = computed(() => this.userSignal()?.role === 'ADMIN');
  readonly billerId = computed(() => this.userSignal()?.id ?? null);

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_BASE_URL}/login`, credentials).pipe(
      tap((response) => this.storeSession(response)),
    );
  }

  /** Answers 201 with an empty body: the caller redirects to the login screen. */
  signup(request: SignupRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/signup`, request);
  }

  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  logout(redirect = true): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.userSignal.set(null);
    if (redirect) {
      void this.router.navigate(['/login']);
    }
  }

  private storeSession(response: LoginResponse): void {
    const user: StoredUser = { id: response.userId, email: response.email, role: response.role };
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSignal.set(user);
  }
}

function readStoredUser(): StoredUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw || !localStorage.getItem(TOKEN_KEY)) {
    return null;
  }
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}
