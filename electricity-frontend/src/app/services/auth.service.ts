import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

/* Interface for User */
export interface AuthUser {
  user_id: string | null;
  temp_uid?: string | null;
  full_name?: string;
  email?: string;
  token?: string;
  delivery_id?: string;
}

// export interface AddressData {
//   zip: string;
//   city: string;
//   city_id?: number;
//   street: string;
//   houseNumber: string;
//   persons?: number;
//   consumption?: number;
// }

/* Storage Keys */
const AUTH_STORAGE_KEY = 'auth_user';
const ADDRESS_KEY = 'address_data';
const PROVIDER_STORAGE_KEY = 'selected_provider';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  /* Platform check for SSR safety */
  private platformId = inject(PLATFORM_ID);

  private addressData: any | null = null;

  /* Internal BehaviorSubject to track auth state */
  private authState$: BehaviorSubject<AuthUser | null> = new BehaviorSubject<AuthUser | null>(null);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) {
    this.loadUserFromStorage();
    this.loadFromStorage();
  }

  /* -------------------------------
     INTERNAL HELPERS
  --------------------------------*/

  /* Check if running in browser */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /* -------------------------------
     INITIALIZATION
  --------------------------------*/

  /* Load user from localStorage on app start */
  private loadUserFromStorage(): void {
    if (!this.isBrowser()) return;

    try {
      const storedUser = localStorage.getItem(AUTH_STORAGE_KEY);

      if (storedUser) {
        const parsedUser: AuthUser = JSON.parse(storedUser);
        this.authState$.next(parsedUser);
      }
    } catch (error) {
      console.error('Error loading user from storage:', error);
      this.clearStorage();
    }
  }

  /* Save user to localStorage */
  private saveUserToStorage(user: AuthUser): void {
    if (!this.isBrowser()) return;

    try {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
    } catch (error) {
      console.error('Error saving user to storage:', error);
    }
  }

  /* Clear localStorage */
  private clearStorage(): void {
    if (!this.isBrowser()) return;

    try {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    } catch (error) {
      console.error('Error clearing storage:', error);
    }
  }

  /* -------------------------------
     AUTH STATE METHODS
  --------------------------------*/

  /* Observable for components to subscribe */
  getAuthState(): Observable<AuthUser | null> {
    return this.authState$.asObservable();
  }

  /* Get current user snapshot */
  getCurrentUser(): AuthUser | null {
    return this.authState$.getValue();
  }

  /* Check if user is logged in */
  isLoggedIn(): boolean {
    const user = this.getCurrentUser();
    return !!(user && user.user_id);
  }

  /* -------------------------------
     LOGIN / LOGOUT
  --------------------------------*/

  /* Login method */
  login(userData: AuthUser): boolean {
    try {
      if (!userData || !userData.user_id) {
        console.error('Invalid login data');
        return false;
      }

      const user: AuthUser = {
        ...userData,
        temp_uid: null,
      };

      this.authState$.next(user);
      this.saveUserToStorage(user);

      return true;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  }

  /* Logout method */
  logout(): void {
    try {
      this.authState$.next(null);
      this.clearStorage();
      this.clearAddress();
      this.clearSelectedProvider();
      this.router.navigate(['/'], { relativeTo: this.route });
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  /* Throw error if user not logged in */
  getUserIdOrThrow(): string {
    const id = this.getUserId();
    if (!id) {
      throw new Error('User not authenticated');
    }
    return id;
  }

  /* -------------------------------
     USER DATA HANDLING
  --------------------------------*/

  /* Update user data */
  updateUser(updatedData: Partial<AuthUser>): void {
    try {
      const currentUser = this.getCurrentUser();

      if (!currentUser) {
        console.warn('No user to update');
        return;
      }

      const updatedUser: AuthUser = {
        ...currentUser,
        ...updatedData,
      };

      this.authState$.next(updatedUser);
      this.saveUserToStorage(updatedUser);
    } catch (error) {
      console.error('Error updating user:', error);
    }
  }

  /* Get user ID (final ID) */
  getUserId(): string | null {
    const user = this.getCurrentUser();
    return user?.user_id || null;
  }

  getUserEmailId(): string | null {
    const user = this.getCurrentUser();
    return user?.email || null;
  }

  /* -------------------------------
     TEMP USER FLOW (MULTI-STEP FORM)
  --------------------------------*/

  /* Set temporary user ID */
  setTempUid(temp_uid: string): void {
    try {
      if (!temp_uid) {
        console.warn('Invalid temp_uid');
        return;
      }

      const currentUser = this.getCurrentUser() || { user_id: null };

      const updatedUser: AuthUser = {
        ...currentUser,
        temp_uid,
      };

      this.authState$.next(updatedUser);
      this.saveUserToStorage(updatedUser);
    } catch (error) {
      console.error('Error setting temp_uid:', error);
    }
  }

  /* Get temporary user ID */
  getTempUid(): string | null {
    const user = this.getCurrentUser();
    return user?.temp_uid || null;
  }

  /* Promote temp_uid to final user_id after registration */
  finalizeUser(finalUserId: string, token?: string): void {
    try {
      if (!finalUserId) {
        console.error('Final user ID is required');
        return;
      }

      const currentUser = this.getCurrentUser();

      const finalizedUser: AuthUser = {
        ...currentUser,
        user_id: finalUserId,
        temp_uid: null,
        token: token || currentUser?.token || undefined /* FIXED */,
      };

      this.authState$.next(finalizedUser);
      this.saveUserToStorage(finalizedUser);
    } catch (error) {
      console.error('Error finalizing user:', error);
    }
  }

  /* -------------------------------
     TOKEN HANDLING
  --------------------------------*/

  /* Get auth token */
  getToken(): string | null {
    const user = this.getCurrentUser();
    return user?.token || null;
  }

  /* Check if token exists */
  hasValidToken(): boolean {
    const token = this.getToken();
    return !!token;
  }

  /* -------------------------------
   DELIVERY ID HANDLING
--------------------------------*/

  /* Save delivery ID */
  setDeliveryId(deliveryId: string): void {
    try {
      const currentUser = this.getCurrentUser();

      if (!currentUser) return;

      const updatedUser = {
        ...currentUser,
        delivery_id: deliveryId,
      };

      this.authState$.next(updatedUser);
      this.saveUserToStorage(updatedUser);
    } catch (error) {
      console.error('Error saving deliveryId:', error);
    }
  }

  /* Get delivery ID */
  getDeliveryId(): string | null {
    const user = this.getCurrentUser();
    return user?.delivery_id || null;
  }

  /// Address save local ///

  /* Load from localStorage */
  private loadFromStorage(): void {
    try {
      const data = localStorage.getItem(ADDRESS_KEY);
      if (data) {
        this.addressData = JSON.parse(data);
      }
    } catch (e) {
      console.error('Error loading address', e);
      this.addressData = null;
    }
  }

  /* Save to localStorage */
  private saveToStorage(data: any): void {
    try {
      localStorage.setItem(ADDRESS_KEY, JSON.stringify(data));
    } catch (e) {
      console.error('Error saving address', e);
    }
  }

  /* Set data */
  setAddressData(data: any): void {
    this.addressData = data;
    this.saveToStorage(data);
  }

  /* Get data */
  getAddressData(): any | null {
    return this.addressData;
  }

  /* Clear data (optional) */
  clearAddress(): void {
    localStorage.removeItem(ADDRESS_KEY);
    this.addressData = null;
  }

  /* Check if exists */
  hasAddress(): boolean {
    return !!(this.addressData?.zip && this.addressData?.city && this.addressData?.street);
  }

  ///---- Select Provider ----///
  setSelectedProvider(provider: any): void {
    if (!this.isBrowser()) return;

    try {
      localStorage.setItem(PROVIDER_STORAGE_KEY, JSON.stringify(provider));
    } catch (error) {
      console.error('Error saving provider:', error);
    }
  }
  getSelectedProvider(): any {
    if (!this.isBrowser()) return null;

    try {
      const data = localStorage.getItem(PROVIDER_STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.error('Error getting provider:', error);
      return null;
    }
  }
  clearSelectedProvider(): void {
    if (!this.isBrowser()) return;

    try {
      localStorage.removeItem(PROVIDER_STORAGE_KEY);
    } catch (error) {
      console.error('Error clearing provider:', error);
    }
  }
}
