import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay, map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ContentService {
  private readonly API_URL = 'http://192.168.0.155:8080/api/content';
  private readonly BASE_IMAGE_URL = 'http://192.168.0.155:8080/assets/super-admin/';

  private data$: Observable<any>;

  constructor(private http: HttpClient) {
    this.data$ = this.http.post<any>(this.API_URL, {}).pipe(
      shareReplay(1)
    );
  }

  // Full data (if needed)
  getData(): Observable<any> {
    return this.data$;
  }

  // NAV
  getNav(): Observable<any[]> {
    return this.data$.pipe(
      map(res => res?.menu?.nav?.sort((a: any, b: any) => a.order - b.order) || [])
    );
  }

  // FREE SERVICE
  getFreeService(): Observable<any[]> {
    return this.data$.pipe(
      map(res => res?.service?.['free-service'] || [])
    );
  }

  // OTHER SERVICE
  getOtherService(): Observable<any[]> {
    return this.data$.pipe(
      map(res => res?.service?.['other-service'] || [])
    );
  }

  // SIDEBAR
  getSidebar(): Observable<any[]> {
    return this.data$.pipe(
      map(res => res?.menu?.sidebar || [])
    );
  }

  // BANNER
  getBanner(): Observable<any[]> {
    return this.data$.pipe(
      map(res => res?.menu?.banner || [])
    );
  }

  // IMAGE URL HELPER
  getImageUrl(contentUrl: string | null): string {
    if (!contentUrl) return '';
    return `${this.BASE_IMAGE_URL}${contentUrl}`;
  }
}