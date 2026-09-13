import { Gateway } from './gateway.model';

export type Urgency = 'INSTANT' | 'CAN_WAIT';

/**
 * A gateway priced for a specific amount, as both /recommend and /split return it: the gateway
 * itself rather than a flattened copy of it, plus what routing worked out for this payment. A
 * recommendation is always a single transaction, so there `requiresSplitting` is false and
 * `splitCount` is 1. The chunk amounts are not repeated here - /split lists them in `splits`.
 */
export interface GatewayCandidate {
  gateway: Gateway;
  /**
   * The biller's daily allowance left on this gateway. On the chosen gateway the payment has
   * already been recorded, so this is what remains after it; on an alternative, nothing was
   * consumed and it is the figure as it stands today.
   */
  remainingQuota: number;
  totalCommission: number;
  requiresSplitting: boolean;
  splitCount: number;
}

/**
 * Body for both /api/payments/recommend and /api/payments/split. One type because the two
 * endpoints take the same thing: a split is a payment in its own right and needs nothing extra.
 */
export interface PaymentRequest {
  /** Optional: omitted means "route for me". Only an admin may name another biller. */
  billerId?: number;
  amount: number;
  urgency: Urgency;
}

export interface RecommendResponse {
  /** Absent when no gateway qualified; the message then explains why. */
  recommendedGateway?: GatewayCandidate;
  /**
   * True only when a gateway was turned away on its per-transaction ceiling alone, meaning the
   * amount may still go through as chunks. Always paired with a null recommendedGateway, so it
   * is a cue to call /split rather than anything about the gateway that was chosen.
   */
  requiresSplitting: boolean;
  alternatives: GatewayCandidate[];
  /** Only sent when nothing was recorded: it explains the refusal. Absent on success. */
  message?: string;
}

export interface SplitResponse {
  /** Absent when no gateway qualified, in which case nothing was recorded. */
  selectedGateway?: GatewayCandidate;
  /** The chunk amounts, in order. Empty when no gateway qualified. */
  splits: number[];
  totalCommission: number;
  /** Only sent when nothing was recorded: it explains the refusal. Absent on success. */
  message?: string;
}
