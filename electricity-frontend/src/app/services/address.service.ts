import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ENVIRONMENT, Environment } from '../environment.token';

export interface CityResponse {
  cityName: string;
  coordinates: number[];
  postcode_localities: string[];
}
export interface City {
  city: string;
  place_id: string;
}

export interface StreetsResponse {
  zip: string;
  city: string;
  streets: string[];
}

@Injectable({ providedIn: 'root' })
export class AddressService {
  private apiUrl = 'http://localhost:8080';

  private baseUrl: string;
  constructor(
    @Inject(ENVIRONMENT) private env: Environment,
    private http: HttpClient,
  ) {
    this.baseUrl = env.apiBaseUrl;
  }

  // getCitiesByZip(zip: string): Observable<{ city: string, city_id: number }[]> {
  //   return this.http.get<any[]>(`${this.baseUrl}/api/address/zipcode`, { params: { zip } }).pipe(
  //     map(response => {
  //       // EGON API returns list of cities with city and city_id
  //       return response.map(item => ({
  //         city: item.city,
  //         city_id: item.city_id
  //       }));
  //     })
  //   );
  // }

  // Get streets by selected city_id

  getCitiesByZip(zip: string): Observable<{ city: string; city_id: string }[]> {
    const url = `https://api.geoapify.com/v1/geocode/search`;

    const params = new HttpParams()
      .set('postcode', zip)
      .set('country', 'germany')
      .set('format', 'json')
      .set('apiKey', '71d6d1808c1d408ca74773b616b93824');

    return this.http.get<any>(url, { params }).pipe(
      map((res) => {
        if (!res.results) return [];

        const uniqueMap = new Map<string, string>();

        res.results.forEach((item: any) => {
          if (item.city && item.place_id) {
            uniqueMap.set(item.city, item.place_id);
          }
        });

        return Array.from(uniqueMap.entries()).map(([city, place_id]) => ({
          city,
          city_id: place_id,
        }));
      }),
    );
  }

  // getStreetsByCity(
  //   streetName: string,
  //   cityId: number,
  // ): Observable<{ street: string; street_id: number }[]> {
  //   return this.http
  //     .get<any[]>(`${this.baseUrl}/api/address/streets`, { params: { streetName, cityId } })
  //     .pipe(
  //       map((response) => {
  //         return response.map((item) => ({
  //           street: item.street,
  //           street_id: item.street_id,
  //         }));
  //       }),
  //     );
  // }

  getStreetsByCity(placeId: string): Observable<{ street: string; street_id: string }[]> {
    const url = `https://api.geoapify.com/v2/places`;

    const params = new HttpParams()
      .set('categories', 'building')
      .set('filter', `place:${placeId}`)
      .set('limit', '300')
      .set('apiKey', '71d6d1808c1d408ca74773b616b93824');

    return this.http.get<any>(url, { params }).pipe(
      map((res) => {
        if (!res.features) return [];

        const streetSet = new Set<string>();

        res.features.forEach((item: any) => {
          const street = item.properties?.street;

          if (street) {
            streetSet.add(street);
          }
        });

        return Array.from(streetSet).map((street, index) => ({
          street,
          street_id: index.toString(),
        }));
      }),
    );
  }

  // this is for open street api

  getCitiesByZipcode(zip: string): Observable<string[]> {
    const params = new HttpParams().set('zip', zip);

    return this.http.get<CityResponse[]>(`${this.baseUrl}/cities`, { params }).pipe(
      map((response) => {
        if (!response || response.length === 0) {
          return [];
        }
        return response.map((city) => city.cityName);
      }),
    );
  }

  getStreetsByZip(zip: string): Observable<string[]> {
    const params = new HttpParams().set('zip', zip);
    return this.http.get<StreetsResponse>(`${this.baseUrl}/streets-by-zip`, { params }).pipe(
      map((response) => {
        if (!response || !response.streets) {
          return [];
        }
        return response.streets;
      }),
    );
  }
}
