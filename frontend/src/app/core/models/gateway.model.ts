export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

/** Every day, in the order the backend returns them. */
export const ALL_DAYS: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

/** Full gateway configuration, as returned by the admin endpoints. */
export interface Gateway {
  id: number;
  name: string;
  fixedCommission: number;
  percentageCommission: number;
  minTransactionAmount: number;
  /** null means the gateway has no per-transaction ceiling ("No Limit"). */
  maxTransactionAmount: number | null;
  dailyLimitPerBiller: number;
  /** "HH:mm:ss" in the business zone. Equal from/to means available around the clock. */
  availableFrom: string;
  availableTo: string;
  /** Inclusive day range that may wrap the week: SUNDAY-THURSDAY, or MONDAY-SUNDAY for every day. */
  availableDayFrom: DayOfWeek;
  availableDayTo: DayOfWeek;
  /** Whole hours. 0 means Instant. */
  processingTimeHours: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

/** Create/update payload. Times are sent as "HH:mm"; omit maxTransactionAmount for no limit. */
export interface GatewayRequest {
  name: string;
  fixedCommission: number;
  percentageCommission: number;
  minTransactionAmount: number;
  maxTransactionAmount: number | null;
  dailyLimitPerBiller: number;
  availableFrom: string;
  availableTo: string;
  availableDayFrom: DayOfWeek;
  availableDayTo: DayOfWeek;
  processingTimeHours: number;
  active: boolean;
}
