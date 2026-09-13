import { DecimalPipe, NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { toErrorMessage } from '../../../core/error-message';
import { limitLabel, speedLabel } from '../../../core/gateway-format';
import { RecommendResponse, SplitResponse, Urgency } from '../../../core/models/payment.model';
import { AuthService } from '../../../core/services/auth.service';
import { PaymentService } from '../../../core/services/payment.service';

@Component({
  selector: 'app-recommend',
  imports: [ReactiveFormsModule, DecimalPipe, NgTemplateOutlet],
  templateUrl: './recommend.html',
  styleUrl: './recommend.css',
})
export class Recommend {
  private readonly payments = inject(PaymentService);
  protected readonly auth = inject(AuthService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly recommendation = signal<RecommendResponse | null>(null);
  protected readonly split = signal<SplitResponse | null>(null);
  // The responses no longer echo the urgency back, so the badge reads what was submitted.
  protected readonly submittedAmount = signal<number | null>(null);
  protected readonly submittedUrgency = signal<Urgency | null>(null);

  protected readonly recommended = computed(() => this.recommendation()?.recommendedGateway ?? null);
  protected readonly alternatives = computed(() => this.recommendation()?.alternatives ?? []);

  // A recommendation that asked for splitting recorded nothing itself, so whether the payment
  // went through at all is decided here and not by the recommendation.
  protected readonly splitSucceeded = computed(() => !!this.split()?.selectedGateway);

  // The split response carries only the plan itself, so the totals the table foots are summed
  // from the chunks rather than echoed back by the server.
  protected readonly splitChunkCount = computed(() => this.split()?.splits.length ?? 0);
  protected readonly splitAmount = computed(() =>
    (this.split()?.splits ?? []).reduce((total, chunk) => total + chunk, 0),
  );

  protected readonly form = inject(FormBuilder).nonNullable.group({
    billerId: [this.auth.billerId() ?? 0, [Validators.required, Validators.min(1)]],
    amount: [1500, [Validators.required, Validators.min(0.01)]],
    urgency: ['INSTANT' as Urgency, [Validators.required]],
  });

  protected submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    const input = this.form.getRawValue();

    this.loading.set(true);
    this.error.set(null);
    this.recommendation.set(null);
    this.split.set(null);
    this.submittedAmount.set(input.amount);
    this.submittedUrgency.set(input.urgency);

    // One call from the component; the service takes care of the follow-up split request
    // when the recommended gateway cannot carry the amount in a single transaction.
    this.payments.recommendWithSplit(input).subscribe({
      next: ({ recommendation, split }) => {
        this.loading.set(false);
        this.recommendation.set(recommendation);
        this.split.set(split);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(toErrorMessage(err, 'Could not route this payment. Please try again.'));
      },
    });
  }

  protected readonly speedLabel = speedLabel;
  protected readonly limitLabel = limitLabel;
}
