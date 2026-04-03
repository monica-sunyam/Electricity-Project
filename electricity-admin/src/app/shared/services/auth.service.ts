import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private LOGIN_KEY = "is_logged_in";
  private USER_ID_KEY = "admin_id";
  private USER_DATA_KEY = "admin_data";

  // BehaviorSubject holds the current user state
  private currentUserSubject = new BehaviorSubject<any>(this.getUserData());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private api: ApiService) {}

  /* ================= LOGIN ================= */

  setLoggedIn(userData: any) {
    localStorage.setItem(this.LOGIN_KEY, "true");
    localStorage.setItem(this.USER_ID_KEY, String(userData.adminId));
    localStorage.setItem(this.USER_DATA_KEY, JSON.stringify(userData));

    // Notify all subscribers (like the header) about the new user
    this.currentUserSubject.next(userData);
  }

  isLoggedIn(): boolean {
    return localStorage.getItem(this.LOGIN_KEY) === "true";
  }

  /* ================= USER ================= */

  private getUserData() {
    const data = localStorage.getItem(this.USER_DATA_KEY);
    return data ? JSON.parse(data) : null;
  }

  setUserId(id: number) {
    localStorage.setItem(this.USER_ID_KEY, String(id));
  }

  getUserId(): number | null {
    const id = localStorage.getItem(this.USER_ID_KEY);
    return id ? Number(id) : null;
  }

  /* ================= API ================= */

  login(data: { email: string; password: string }) {
    return this.api.post("admin/admin-login", data);
  }

  logoutApi() {
    const adminId = this.getUserId();
    return this.api.post("admin/admin-logout", {
      adminId: adminId,
    });
  }

  /* ================= LOGOUT ================= */

  logout() {
    localStorage.removeItem(this.LOGIN_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    localStorage.removeItem(this.USER_DATA_KEY);
    this.currentUserSubject.next(null);
  }
}
