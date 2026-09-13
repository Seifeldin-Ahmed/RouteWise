import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { toErrorMessage } from '../../../core/error-message';
import { GatewayBreakdown, TransactionHistory } from '../../../core/models/transaction.model';
import { AuthService } from '../../../core/services/auth.service';
import { TransactionService } from '../../../core/services/transaction.service';

@Component({
  selector: 'app-history',
  imports: [ReactiveFormsModule, DecimalPipe, DatePipe],
  templateUrl: './history.html',
  styleUrl: './history.css',
})
export class History {
  private readonly transactions = inject(TransactionService);
  protected readonly auth = inject(AuthService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly history = signal<TransactionHistory | null>(null);

  /**
   * Gateways offered in the filter, taken from the rows already loaded. The gateway
   * catalogue endpoint is admin-only, and a biller can only filter by gateways they have
   * actually used anyway.
   */
  protected readonly gatewayChoices = computed(() => {
    const rows = this.history()?.gatewayBreakdown ?? [];
    return rows.map((row) => ({ id: row.gatewayId, name: row.gatewayName }));
  });

  /**
   * What is left of today's limit on this gateway. The amount processed is the quota used, but
   * only for a single day - over a wider range it sums several days and subtracting it from a
   * daily limit would be meaningless, so the column stays blank.
   */
  protected quotaLeft(row: GatewayBreakdown): number | null {
    if (!this.history()?.date) {
      return null;
    }
    return Math.max(row.dailyLimitPerBiller - row.totalAmount, 0);
  }

  protected readonly form = inject(FormBuilder).nonNullable.group({
    billerId: [this.auth.billerId() ?? 0, [Validators.required, Validators.min(1)]],
    date: [''],
    gatewayId: [''],
  });

  constructor() {
    this.load();
  }

  /**
   * Only the transaction rows are paged; the totals and the breakdown always cover the whole
   * filter. Defaulting to page 0 means applying or clearing a filter starts from the top again.
   */
  protected load(page = 0): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    const { billerId, date, gatewayId } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);

    this.transactions
      .history(billerId, {
        date: date || null,
        gatewayId: gatewayId ? Number(gatewayId) : null,
        page,
      })
      .subscribe({
        next: (result) => {
          this.loading.set(false);
          this.history.set(result);
        },
        error: (err) => {
          this.loading.set(false);
          this.history.set(null);
          this.error.set(toErrorMessage(err, 'Could not load your payment history. Please try again.'));
        },
      });
  }

  protected clearFilters(): void {
    this.form.patchValue({ date: '', gatewayId: '' });
    this.load();
  }
}
