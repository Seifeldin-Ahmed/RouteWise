import { ALL_DAYS, DayOfWeek } from './models/gateway.model';

const SHORT_DAY: Record<DayOfWeek, string> = {
  MONDAY: 'Mon',
  TUESDAY: 'Tue',
  WEDNESDAY: 'Wed',
  THURSDAY: 'Thu',
  FRIDAY: 'Fri',
  SATURDAY: 'Sat',
  SUNDAY: 'Sun',
};

/** Business week order: the Egyptian week runs Sunday to Thursday. */
const WEEK_ORDER: DayOfWeek[] = [
  'SUNDAY',
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
];

/** "Instant" for 0, otherwise whole hours. */
export function speedLabel(hours: number): string {
  if (hours === 0) {
    return 'Instant';
  }
  return hours === 1 ? '1 hour' : `${hours} hours`;
}

/** "No limit" when the gateway has no per-transaction ceiling. */
export function limitLabel(amount: number | null): string {
  return amount === null || amount === undefined ? 'No limit' : amount.toFixed(2);
}

/** "00:00 - 00:00" is stored to mean the gateway never closes. */
export function windowLabel(from: string, to: string): string {
  const start = from.slice(0, 5);
  const end = to.slice(0, 5);
  if (start === end) {
    return '24 hours';
  }
  return start > end ? `${start} - ${end} (overnight)` : `${start} - ${end}`;
}

/**
 * Renders the inclusive day range: "Every day" when it spans the whole week, "Fri" for a
 * single day, otherwise "Sun-Thu".
 */
export function daysLabel(from: DayOfWeek | null | undefined, to: DayOfWeek | null | undefined): string {
  if (!from || !to) {
    return 'Never';
  }
  const span = (ALL_DAYS.indexOf(to) - ALL_DAYS.indexOf(from) + 7) % 7;
  if (span === ALL_DAYS.length - 1) {
    return 'Every day';
  }
  if (span === 0) {
    return SHORT_DAY[from];
  }
  return `${SHORT_DAY[from]}-${SHORT_DAY[to]}`;
}

/** Full availability summary: days plus the daily window. */
export function availabilityLabel(
  dayFrom: DayOfWeek | null | undefined,
  dayTo: DayOfWeek | null | undefined,
  from: string,
  to: string,
): string {
  const window = windowLabel(from, to);
  const dayPart = daysLabel(dayFrom, dayTo);
  if (dayPart === 'Every day' && window === '24 hours') {
    return '24/7';
  }
  return `${dayPart}, ${window}`;
}

export { SHORT_DAY, WEEK_ORDER };
