import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { toErrorMessage } from '../../../core/error-message';
import { availabilityLabel, limitLabel, speedLabel, WEEK_ORDER, SHORT_DAY } from '../../../core/gateway-format';
import { DayOfWeek, Gateway, GatewayRequest } from '../../../core/models/gateway.model';
import { GatewayService } from '../../../core/services/gateway.service';

const BLANK_FORM = {
  name: '',
  fixedCommission: 2,
  percentageCommission: 1.5,
  minTransactionAmount: 10,
  maxTransactionAmount: 5000 as number | null,
  noMaximum: false,
  dailyLimitPerBiller: 50000,
  availableFrom: '00:00',
  availableTo: '00:00',
  availableDayFrom: 'MONDAY' as DayOfWeek,
  availableDayTo: 'SUNDAY' as DayOfWeek,
  processingTimeHours: 0,
  active: true,
};

@Component({
  selector: 'app-gateways',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './gateways.html',
  styleUrl: './gateways.css',
})
export class Gateways {
  private readonly gateways = inject(GatewayService);

  protected readonly rows = signal<Gateway[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly notice = signal<string | null>(null);
  /** Null means the form is in "create" mode. */
  protected readonly editingId = signal<number | null>(null);

  protected readonly weekOrder = WEEK_ORDER;
  protected readonly shortDay = SHORT_DAY;
  protected readonly speedLabel = speedLabel;
  protected readonly limitLabel = limitLabel;
  protected readonly availabilityLabel = availabilityLabel;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    name: [BLANK_FORM.name, [Validators.required, Validators.maxLength(120)]],
    fixedCommission: [BLANK_FORM.fixedCommission, [Validators.required, Validators.min(0)]],
    percentageCommission: [BLANK_FORM.percentageCommission, [Validators.required, Validators.min(0)]],
    minTransactionAmount: [BLANK_FORM.minTransactionAmount, [Validators.required, Validators.min(0.01)]],
    maxTransactionAmount: [BLANK_FORM.maxTransactionAmount, [Validators.min(0.01)]],
    noMaximum: [BLANK_FORM.noMaximum],
    dailyLimitPerBiller: [BLANK_FORM.dailyLimitPerBiller, [Validators.required, Validators.min(0.01)]],
    availableFrom: [BLANK_FORM.availableFrom, [Validators.required]],
    availableTo: [BLANK_FORM.availableTo, [Validators.required]],
    availableDayFrom: [BLANK_FORM.availableDayFrom, [Validators.required]],
    availableDayTo: [BLANK_FORM.availableDayTo, [Validators.required]],
    processingTimeHours: [BLANK_FORM.processingTimeHours, [Validators.required, Validators.min(0)]],
    active: [BLANK_FORM.active],
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.gateways.findAll(true).subscribe({
      next: (rows) => {
        this.loading.set(false);
        this.rows.set(rows);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(toErrorMessage(err, 'Could not load the gateways. Please try again.'));
      },
    });
  }

  protected edit(gateway: Gateway): void {
    this.editingId.set(gateway.id);
    this.notice.set(null);
    this.error.set(null);
    this.form.setValue({
      name: gateway.name,
      fixedCommission: gateway.fixedCommission,
      percentageCommission: gateway.percentageCommission,
      minTransactionAmount: gateway.minTransactionAmount,
      // "No Limit" arrives as an absent field, not an explicit null, so test loosely.
      maxTransactionAmount: gateway.maxTransactionAmount ?? null,
      noMaximum: gateway.maxTransactionAmount == null,
      dailyLimitPerBiller: gateway.dailyLimitPerBiller,
      availableFrom: gateway.availableFrom.slice(0, 5),
      availableTo: gateway.availableTo.slice(0, 5),
      availableDayFrom: gateway.availableDayFrom,
      availableDayTo: gateway.availableDayTo,
      processingTimeHours: gateway.processingTimeHours,
      active: gateway.active,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected resetForm(): void {
    this.editingId.set(null);
    this.form.setValue({ ...BLANK_FORM });
    this.error.set(null);
  }

  protected submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    // A gateway with no ceiling has nothing to compare the minimum against.
    const maxTransactionAmount = raw.noMaximum ? null : raw.maxTransactionAmount;
    if (maxTransactionAmount !== null && maxTransactionAmount < raw.minTransactionAmount) {
      this.error.set('The maximum per transaction must be at least the minimum.');
      return;
    }

    // On a one-day range a closing time before the opening one runs backwards, not overnight.
    if (raw.availableDayFrom === raw.availableDayTo && raw.availableFrom > raw.availableTo) {
      this.error.set(
        `The closing time is earlier than the opening time on ${SHORT_DAY[raw.availableDayFrom]}. ` +
          'For an overnight window, set the last day to the day it ends on.',
      );
      return;
    }

    const payload: GatewayRequest = {
      name: raw.name,
      fixedCommission: raw.fixedCommission,
      percentageCommission: raw.percentageCommission,
      minTransactionAmount: raw.minTransactionAmount,
      maxTransactionAmount,
      dailyLimitPerBiller: raw.dailyLimitPerBiller,
      availableFrom: raw.availableFrom,
      availableTo: raw.availableTo,
      availableDayFrom: raw.availableDayFrom,
      availableDayTo: raw.availableDayTo,
      processingTimeHours: raw.processingTimeHours,
      active: raw.active,
    };

    this.saving.set(true);
    this.error.set(null);
    this.notice.set(null);

    const id = this.editingId();
    const request$ = id === null ? this.gateways.create(payload) : this.gateways.update(id, payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.notice.set(
          id === null ? `Gateway "${raw.name}" created.` : `Gateway "${raw.name}" updated.`,
        );
        this.resetForm();
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(toErrorMessage(err, 'Could not save the gateway. Please try again.'));
      },
    });
  }

  protected toggleActive(gateway: Gateway): void {
    const activating = !gateway.active;

    this.gateways.setActive(gateway.id, activating).subscribe({
      next: () => {
        this.notice.set(
          activating
            ? `Gateway "${gateway.name}" is routable again.`
            : `Gateway "${gateway.name}" deactivated. History is untouched.`,
        );
        this.load();
      },
      error: (err) => this.error.set(toErrorMessage(err, 'Could not change the gateway state. Please try again.')),
    });
  }

  protected remove(gateway: Gateway): void {
    // Deleting takes the history with it, so the warning has to say so before anything happens.
    const warning =
      `Delete "${gateway.name}" for good?\n\n` +
      'This gateway may already have transactions recorded against it. Every one of them will ' +
      'be deleted too, along with each biller\'s daily allowance on it, and none of it can be ' +
      'recovered.\n\n' +
      'To stop it routing payments but keep its history, deactivate it instead.';

    if (!confirm(warning)) {
      return;
    }

    this.gateways.remove(gateway.id).subscribe({
      next: () => {
        // The form may be sitting on the row that just went away.
        if (this.editingId() === gateway.id) {
          this.resetForm();
        }
        this.notice.set(`Gateway "${gateway.name}" deleted, along with its transactions.`);
        this.load();
      },
      error: (err) => this.error.set(toErrorMessage(err, 'Could not delete the gateway. Please try again.')),
    });
  }
}
