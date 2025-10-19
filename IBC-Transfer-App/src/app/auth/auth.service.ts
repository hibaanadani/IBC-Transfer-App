import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // ⚠️ CRITICAL CHANGE: Using the full backend URL to bypass the need for an Angular proxy
  // This assumes your Spring Boot backend runs on port 8080.
  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private isBrowser: boolean;
  private readonly TOKEN_KEY = 'jwt_token'; // Standardized token key

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  private getStorage(): Storage | null {
    return this.isBrowser ? localStorage : null;
  }

  public getToken(): string | null {
    const storage = this.getStorage();
    return storage ? storage.getItem(this.TOKEN_KEY) : null;
  }

  public saveToken(token: string): void {
    const storage = this.getStorage();
    if (storage) {
      storage.setItem(this.TOKEN_KEY, token);
    }
  }

  public hasToken(): boolean {
    const storage = this.getStorage();
    return !!storage && !!storage.getItem(this.TOKEN_KEY);
  }

  public removeToken(): void {
    const storage = this.getStorage();
    if (storage) {
      storage.removeItem(this.TOKEN_KEY);
    }
  }

  // NEW METHOD: Safely decode and extract the username
  public getUsernameFromToken(): string | null {
    const token = this.getToken();

    if (this.isBrowser && token) {
      try {
        const decodedToken: any = jwtDecode(token);
        // Assumes username is stored in the 'sub' claim
        return decodedToken.sub || null;
      } catch (error) {
        console.error('Error decoding token:', error);
        this.removeToken();
        return null;
      }
    }
    return null;
  }

  login(credentials: any): Observable<any> {
    // Now calls http://localhost:8080/api/auth/login
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  register(payload: any): Observable<any> {
    // Now calls http://localhost:8080/api/auth/register
    return this.http.post(`${this.apiUrl}/register`, payload);
  }
}
