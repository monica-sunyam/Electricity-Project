import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs'; // Added 'of'
import { map, catchError } from 'rxjs/operators'; // Added 'catchError'
import { ENVIRONMENT, Environment } from '../environment.token';

// export interface CityResponse {
//   cityName: string;
//   coordinates: number[];
//   postcode_localities: string[];
// }

export interface CityResponse {
  res: boolean;
  data: City[];
}

export interface StreetResponse {
  res: boolean;
  data: Street[];
}
export interface City {
  city: string;
  city_id: string;
}

export interface Street {
  street: string;
  street_id: string;
}

export interface StreetsResponse {
  zip: string;
  city: string;
  streets: string[];
}


@Injectable({ providedIn: 'root' })
export class AddressService {
  private baseUrl: string;

  constructor(
    @Inject(ENVIRONMENT) private env: Environment,
    private http: HttpClient,
  ) {
    this.baseUrl = env.apiBaseUrl;
  }

  getCitiesByZip(zip: string): Observable<{ city: string; city_id: string }[]> {
    const url = `/api/cities`;

    return this.http.post<CityResponse>(this.baseUrl + url, { zip }).pipe(
      map((response) => {
        if (response && response.res && Array.isArray(response.data)) {
          return response.data.map((item: City) => ({
            city: item.city,
            city_id: item.city_id
          }));
        }
        return [];
      }),
      catchError((error: any) => {
        console.error('Error fetching cities:', error);
        return of([]);
      })
    );
  }

  getStreetsByCity(placeId: string): Observable<{ street: string; street_id: string }[]> {
    const url = `/api/streets-by-zip`;

    return this.http.post<StreetResponse>(this.baseUrl + url, { placeId }).pipe(
      map((res) => {
        if (res && res.res && Array.isArray(res.data)) {
          return res.data.map((item: Street) => ({
            street: item.street,
            street_id: item.street_id.toString()
          }));
        }
        return [];
      }),
      catchError((error: any) => {
        console.error('Error fetching streets:', error);
        return of([]);
      })
    );
  }
}