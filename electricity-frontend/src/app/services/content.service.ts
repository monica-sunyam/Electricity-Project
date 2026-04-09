import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ContentService {
  private readonly API_URL = 'http://192.168.0.155:8080/api/content';
  private readonly BASE_IMAGE_URL = 'http://192.168.0.155:8080/assets/super-admin/';

  // shareReplay(1) caches the result — all components share one HTTP call
  private data$: Observable<any>;

  constructor(private http: HttpClient) {
    this.data$ = this.http.post<any>(this.API_URL, {}).pipe(shareReplay(1));
  }

  getData(): Observable<any> {
    return this.data$;
  }

  getImageUrl(contentUrl: string): string {
    return `${this.BASE_IMAGE_URL}${contentUrl}`;
  }
}
