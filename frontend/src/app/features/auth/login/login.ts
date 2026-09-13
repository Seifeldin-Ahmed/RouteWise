import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { toErrorMessage } from '../../../core/error-message';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo') ?? '/payments';
        void this.router.navigateByUrl(redirectTo);
      },
      error: (err) => {
        this.submitting.set(false);
        this.error.set(toErrorMessage(err, 'Could not sign you in. Please try again.'));
      },
    });
  }

  /** Fills the form with a seeded account so the app can be tried immediately. */
  protected useDemoAccount(role: 'admin' | 'biller'): void {
    this.form.setValue(
      role === 'admin'
        ? { email: 'admin@fawry.com', password: 'Admin@123' }
        : { email: 'biller@fawry.com', password: 'User@123' },
    );
  }
}
