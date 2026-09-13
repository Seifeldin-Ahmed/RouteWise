import { DecimalPipe } from '@angular/common';
import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Urgency } from '../../core/models/payment.model';
import { AuthService } from '../../core/services/auth.service';

/**
 * A gateway in the playground. The figures are invented, but every rule applied to them below is
 * a port of the real one - AvailabilityChecker, SplitCalculator, CommissionCalculator and
 * GatewayRanker - so the board never shows a routing the service would not have made.
 */
interface DemoGateway {
  id: number;
  name: string;
  blurb: string;
  fixed: number;
  percentage: number;
  hours: number;
  min: number;
  max: number;
  limit: number;
  used: number;
  /** ISO day numbers, Monday 1 through Sunday 7, both ends inclusive. */
  dayFrom: number;
  dayTo: number;
  /** Minutes past midnight. Equal ends mean the gateway never closes. */
  openFrom: number;
  openTo: number;
}

/** Which rule turned a gateway away, kept as a code so the rules section can count them. */
type Blocked = 'hours' | 'quota' | 'min' | 'cap';

/** One line of the board: a gateway plus what the rules just decided about it. */
interface Row {
  gateway: DemoGateway;
  ranked: boolean;
  blocked: Blocked | null;
  reason: string;
  chunks: number[];
  commission: number;
}

const at = (hour: number) => hour * 60;

const GATEWAYS: DemoGateway[] = [
  { id: 1, name: 'MicroPay', blurb: 'Top-ups and small bills',
    fixed: 0.5, percentage: 2.2, hours: 1, min: 10, max: 3000, limit: 25000, used: 0,
    dayFrom: 1, dayTo: 7, openFrom: 0, openTo: 0 },
  { id: 2, name: 'ClearLink', blurb: 'Everyday transfers',
    fixed: 2.5, percentage: 0.9, hours: 2, min: 100, max: 15000, limit: 30000, used: 9000,
    dayFrom: 1, dayTo: 7, openFrom: 0, openTo: 0 },
  { id: 3, name: 'SwiftPay', blurb: 'Settles on the spot',
    fixed: 5, percentage: 1.5, hours: 0, min: 50, max: 20000, limit: 60000, used: 0,
    dayFrom: 1, dayTo: 7, openFrom: 0, openTo: 0 },
  { id: 4, name: 'BulkClear', blurb: 'Built for large volume',
    fixed: 8, percentage: 0.35, hours: 6, min: 2000, max: 25000, limit: 100000, used: 0,
    dayFrom: 1, dayTo: 7, openFrom: 0, openTo: 0 },
  { id: 5, name: 'WeekdayWire', blurb: 'Banking hours only',
    fixed: 3, percentage: 1.1, hours: 3, min: 100, max: 10000, limit: 40000, used: 0,
    dayFrom: 1, dayTo: 5, openFrom: at(9), openTo: at(17) },
  { id: 6, name: 'NightOwl', blurb: 'Cheap after hours',
    fixed: 1, percentage: 0.6, hours: 12, min: 50, max: 20000, limit: 80000, used: 0,
    dayFrom: 1, dayTo: 7, openFrom: at(22), openTo: at(6) },
];

const SPLIT_EXAMPLE_AMOUNT = 25100;

const round2 = (value: number) => Math.round(value * 100) / 100;

/** Port of AvailabilityChecker: the time window first, then the day the session belongs to. */
function isOpen(gateway: DemoGateway, now: Date): boolean {
  const minutes = now.getHours() * 60 + now.getMinutes();
  const { openFrom, openTo } = gateway;
  const crossesMidnight = openFrom > openTo;
  const inWindow = openFrom === openTo
    ? true
    : crossesMidnight
      ? minutes >= openFrom || minutes < openTo
      : minutes >= openFrom && minutes < openTo;
  if (!inWindow) {
    return false;
  }
  // Only a window running past midnight can belong to yesterday, and only before it opens again.
  const today = now.getDay() === 0 ? 7 : now.getDay();
  const day = crossesMidnight && minutes < openFrom ? ((today + 5) % 7) + 1 : today;
  const span = (((gateway.dayTo - gateway.dayFrom) % 7) + 7) % 7;
  const offset = (((day - gateway.dayFrom) % 7) + 7) % 7;
  return offset <= span;
}

/** Port of SplitCalculator: fewest chunks that fit, then pull the last one up to the minimum. */
function splitPlan(amount: number, min: number, max: number): number[] | null {
  // The epsilon keeps an exact multiple at its own chunk count instead of adding an empty one.
  const count = Math.ceil(amount / max - 1e-9);
  if (count > 1000 || min * count > amount) {
    return null;
  }

  const chunks = new Array<number>(count).fill(max);
  chunks[count - 1] = round2(amount - max * (count - 1));

  for (let i = count - 1; i >= 0; i--) {
    let shortfall = round2(min - chunks[i]);
    if (shortfall <= 0) {
      break;
    }
    for (let donor = i - 1; donor >= 0 && shortfall > 0; donor--) {
      const spare = round2(chunks[donor] - min);
      if (spare <= 0) {
        break;
      }
      const taken = Math.min(spare, shortfall);
      chunks[donor] = round2(chunks[donor] - taken);
      chunks[i] = round2(chunks[i] + taken);
      shortfall = round2(shortfall - taken);
    }
  }

  return chunks.every((chunk) => chunk >= min && chunk <= max) ? chunks : null;
}

/** Port of CommissionCalculator: the fixed fee lands on every chunk, rounded chunk by chunk. */
function commissionFor(gateway: DemoGateway, chunks: number[]): number {
  return round2(chunks.reduce(
    (total, chunk) => total + round2(gateway.fixed + (chunk * gateway.percentage) / 100), 0));
}

@Component({
  selector: 'app-landing',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  protected readonly auth = inject(AuthService);

  private readonly console = viewChild<ElementRef<HTMLElement>>('console');

  protected readonly amount = signal(12000);
  protected readonly urgency = signal<Urgency>('CAN_WAIT');

  /** Re-read on every change so a gateway's opening hours track the visitor's own clock. */
  private readonly now = signal(new Date());

  /**
   * The whole routing decision for the amount on the slider. When nothing can take the payment
   * whole, the board falls back to split plans - the same two-step the frontend makes against
   * /recommend and then /split.
   */
  protected readonly board = computed(() => {
    const amount = this.amount();
    const now = this.now();
    const assessed = GATEWAYS.map((gateway) => this.assess(gateway, amount, now));

    const whole = assessed.filter((row) => row.ranked && row.chunks.length === 1);
    const splitting = whole.length === 0;
    const ranked = (splitting ? assessed.filter((row) => row.ranked) : whole).sort(this.order());
    const turnedAway = assessed.filter((row) => !ranked.includes(row));

    return { rows: [...ranked, ...turnedAway], ranked, turnedAway, splitting,
      chosen: ranked[0] ?? null };
  });

  /**
   * The alternative worth naming: against a fast winner, the cheapest route it passed over;
   * against a cheap one, the quickest. Null when nothing else qualified.
   */
  protected readonly comparedWith = computed(() => {
    const others = this.board().ranked.slice(1);
    if (others.length === 0) {
      return null;
    }
    return this.urgency() === 'INSTANT'
      ? others.reduce((best, row) => (row.commission < best.commission ? row : best))
      : others.reduce((best, row) => (row.gateway.hours < best.gateway.hours ? row : best));
  });

  /** Always the premium paid for speed, or the saving made by waiting - never negative. */
  protected readonly difference = computed(() => {
    const other = this.comparedWith();
    return other ? Math.abs(round2(this.board().chosen!.commission - other.commission)) : 0;
  });

  /** How many gateways each rule turned away just now, so the rules read as live, not decorative. */
  protected readonly knockedOut = computed(() => {
    // Counted over the rejects only: in split mode the cap no longer disqualifies anyone.
    const { turnedAway, ranked } = this.board();
    const count = (kind: Blocked) => turnedAway.filter((row) => row.blocked === kind).length;
    return { hours: count('hours'), range: count('min') + count('cap'), quota: count('quota'),
      ranked: ranked.length };
  });

  /** Worked out once with the real algorithm, so the splitting section can never drift from it. */
  protected readonly example = (() => {
    const gateway = GATEWAYS.find((candidate) => candidate.name === 'BulkClear')!;
    const chunks = splitPlan(SPLIT_EXAMPLE_AMOUNT, gateway.min, gateway.max) ?? [];
    return { amount: SPLIT_EXAMPLE_AMOUNT, gateway, chunks };
  })();

  protected setAmount(value: number | string): void {
    this.amount.set(Number(value));
    this.now.set(new Date());
  }

  protected setUrgency(urgency: Urgency): void {
    this.urgency.set(urgency);
  }

  protected showExample(): void {
    this.setAmount(this.example.amount);
    this.console()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  protected speed(hours: number): string {
    return hours === 0 ? 'Instant' : `${hours} h`;
  }

  /** Share of the daily allowance already spent, and the share this payment would take. */
  protected share(row: Row, part: 'used' | 'now'): number {
    const { limit, used } = row.gateway;
    const spent = (used / limit) * 100;
    return part === 'used' ? spent : Math.min(100 - spent, (this.amount() / limit) * 100);
  }

  private order(): (a: Row, b: Row) => number {
    return this.urgency() === 'INSTANT'
      ? (a, b) => a.gateway.hours - b.gateway.hours || a.commission - b.commission
        || a.gateway.id - b.gateway.id
      : (a, b) => a.commission - b.commission || a.gateway.hours - b.gateway.hours
        || a.gateway.id - b.gateway.id;
  }

  /** The four filters, in the order RoutingService applies them, then the price. */
  private assess(gateway: DemoGateway, amount: number, now: Date): Row {
    const out = (blocked: Blocked, reason: string): Row =>
      ({ gateway, ranked: false, blocked, reason, chunks: [], commission: 0 });

    if (!isOpen(gateway, now)) {
      return out('hours', gateway.dayTo < 7 ? 'Closed - weekdays, 09:00 to 17:00'
        : 'Closed - opens at 22:00');
    }

    const remaining = gateway.limit - gateway.used;
    if (remaining < amount) {
      return out('quota', `Only ${remaining.toLocaleString('en-US')} EGP left of today's allowance`);
    }
    if (amount < gateway.min) {
      return out('min', `Under its ${gateway.min.toLocaleString('en-US')} EGP minimum`);
    }
    if (amount <= gateway.max) {
      return { gateway, ranked: true, blocked: null, reason: '', chunks: [amount],
        commission: commissionFor(gateway, [amount]) };
    }

    const chunks = splitPlan(amount, gateway.min, gateway.max);
    const cap = `Over its ${gateway.max.toLocaleString('en-US')} EGP cap`;
    if (!chunks) {
      return out('cap', `${cap}, and no split fits either`);
    }
    return { gateway, ranked: true, blocked: 'cap', reason: `${cap} - ${chunks.length} parts`,
      chunks, commission: commissionFor(gateway, chunks) };
  }
}
