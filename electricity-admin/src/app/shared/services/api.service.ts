import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class ApiService {
  private BASE_URL = "http://localhost:8080"; /* change later */

  constructor(private http: HttpClient) { }

  get(endpoint: string, params?: any): Observable<any> {
    return this.http.get(`${this.BASE_URL}/${endpoint}`, { params });
  }

  post(endpoint: string, body: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/${endpoint}`, body);
  }

  extractErrorMessage(err: any): string {
    if (err?.error?.res === false && err?.error?.errorMessage) {
      return err.error.errorMessage;
    }
    return "Something went wrong";
  }
}
