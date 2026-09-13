import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api.config';
import { HistoryFilters, TransactionHistory } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);

  /**
   * Daily history for one biller. A USER may only pass their own id - the backend rejects
   * anything else with 403 - while an ADMIN may query any biller.
   */
  history(billerId: number, filters: HistoryFilters = {}): Observable<TransactionHistory> {
    let params = new HttpParams();
    if (filters.date) {
      params = params.set('date', filters.date);
    }
    if (filters.gatewayId !== null && filters.gatewayId !== undefined) {
      params = params.set('gatewayId', filters.gatewayId);
    }
    if (filters.page) {
      params = params.set('page', filters.page);
    }
    return this.http.get<TransactionHistory>(
      `${API_BASE_URL}/api/billers/${billerId}/transactions`,
      { params },
    );
  }
}
