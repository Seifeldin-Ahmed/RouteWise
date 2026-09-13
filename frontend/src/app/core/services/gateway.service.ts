import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api.config';
import { Gateway, GatewayRequest } from '../models/gateway.model';

/** Admin gateway configuration. Every call here needs an ADMIN token. */
@Injectable({ providedIn: 'root' })
export class GatewayService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/api/gateways`;

  findAll(includeInactive = true): Observable<Gateway[]> {
    return this.http.get<Gateway[]>(this.baseUrl, { params: { includeInactive } });
  }

  // The writes answer 200 with an empty body, so the caller re-reads the list afterwards
  // rather than patching a returned row into it.

  create(request: GatewayRequest): Observable<void> {
    return this.http.post<void>(this.baseUrl, request);
  }

  update(id: number, request: GatewayRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, request);
  }

  /** Soft: the gateway stops being routable but history keeps resolving it. */
  setActive(id: number, active: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/active`, {}, { params: { active } });
  }

  /** Hard delete. Answers 409 once the gateway has transactions on it. */
  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
