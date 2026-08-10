import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { DashboardStats } from '../models/dashboard.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/v1/dashboard`;
  private readonly TTL = 30_000;

  private cache: DashboardStats | null = null;
  private cacheAt = 0;

  getStats(forceRefresh = false): Observable<DashboardStats> {
    const fresh = Date.now() - this.cacheAt < this.TTL;
    if (!forceRefresh && fresh && this.cache) {
      return of(this.cache);
    }
    return this.http.get<DashboardStats>(`${this.url}/stats`).pipe(
      tap((data) => {
        this.cache = data;
        this.cacheAt = Date.now();
      }),
    );
  }

  invalidateCache(): void {
    this.cache = null;
    this.cacheAt = 0;
  }
}
