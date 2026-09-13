import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';

import { API_BASE_URL } from '../api.config';
import { PaymentRequest, RecommendResponse, SplitResponse } from '../models/payment.model';

/** Everything under /api/payments. Components never call HttpClient themselves. */
@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);

  recommend(request: PaymentRequest): Observable<RecommendResponse> {
    return this.http.post<RecommendResponse>(`${API_BASE_URL}/api/payments/recommend`, request);
  }

  split(request: PaymentRequest): Observable<SplitResponse> {
    return this.http.post<SplitResponse>(`${API_BASE_URL}/api/payments/split`, request);
  }

  /**
   * The flow the recommend screen uses: ask for a recommendation and, when the amount does
   * not fit the chosen gateway in one go, fetch the chunk breakdown straight away.
   *
   * <p>A recommendation that sets requiresSplitting selected no gateway and recorded nothing,
   * so the follow-up split is the only payment made and needs nothing carried over from it.</p>
   */
  recommendWithSplit(
    request: PaymentRequest,
  ): Observable<{ recommendation: RecommendResponse; split: SplitResponse | null }> {
    return this.recommend(request).pipe(
      switchMap((recommendation) => {
        if (!recommendation.requiresSplitting) {
          return of({ recommendation, split: null });
        }
        return this.split({ ...request }).pipe(
          switchMap((split) => of({ recommendation, split })),
        );
      }),
    );
  }
}
