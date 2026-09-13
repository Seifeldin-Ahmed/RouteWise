import { Gateway } from './gateway.model';
import { Urgency } from './payment.model';

/**
 * One transaction row, sent as the entity itself so the gateway arrives nested rather than
 * flattened into an id and a name. A payment the engine split arrives as several of these,
 * one per chunk, and nothing distinguishes them from a payment that went through whole.
 */
export interface Transaction {
  id: number;
  gateway: Gateway;
  amount: number;
  commission: number;
  urgency: Urgency;
  businessDate: string;
  createdAt: string;
}

export interface GatewayBreakdown {
  gatewayId: number;
  gatewayName: string;
  /** Transaction rows: a payment split four ways counts four times here. */
  transactionCount: number;
  /** On a single-day query this is also the quota used, so the backend sends it only once. */
  totalAmount: number;
  totalCommission: number;
  dailyLimitPerBiller: number;
}

export interface TransactionHistory {
  billerId: number;
  /** The filters echoed back. Absent from the JSON, not null, when the filter was not used. */
  date?: string;
  gatewayId?: number;
  totalTransactions: number;
  totalAmountProcessed: number;
  totalCommissionCharged: number;
  gatewayBreakdown: GatewayBreakdown[];
  /** Only `transactions` is paged - every total above it covers the whole filter. */
  page: number;
  totalPages: number;
  transactions: Transaction[];
}

export interface HistoryFilters {
  date?: string | null;
  gatewayId?: number | null;
  page?: number;
}
