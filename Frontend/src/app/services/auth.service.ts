import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

export interface LoginResponse {
  success?: boolean;
  message?: string;
  data?: {
    accessToken: string;
    access_token: string;
    token: string;
    user?: UserProfile;
  };

  accessToken?: string;
  access_token?: string;
  token?: string;
}

export interface UserProfile {
  id?: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
}

export interface ApiResponse<T> {
  success?: boolean;
  data?: T;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private api = 'http://localhost:8088/api/v1/auth';
  private tokenKey = 'authToken';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());

  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private http = inject(HttpClient);
  private router = inject(Router);

  authenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  private handleError (error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';

    if(error.error instanceof ErrorEvent) {
      //client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      //server-side error
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }

    console.error('Authenticated Service Error:',errorMessage);
    return throwError(() => ({message: errorMessage, status: error.status}) );
  }

  saveToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey)
  }

  getUserRole(): string | null {
    const token = this.getToken();
    if(!token) return 'guest';

    try {
      const decoded: any = jwtDecode(token);
      return decoded.role || decoded.roles || 'user';
    } catch {
      return 'user';
    }
  }

  register(credentials: {username: string; email: string; password: string}): Observable<any>{
    return this.http.post(this.api + '/register', credentials).pipe(
      catchError(this.handleError)
    );
  }

  login(credentials: {username: string; password: string}): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.api + '/login', credentials, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      tap(response => {
        let token: string | undefined;
        if(response.data) {
          token = response.data.accessToken || response.data.access_token || response.data.token;
        }

        if(token) {
          this.saveToken(token);
          this.isAuthenticatedSubject.next(true);
        } else {
          console.log('No token received in response', response);
          throw new Error('No authentication token received from server');
        }
      }),
      catchError(this.handleError.bind(this))
    );    
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  loginTest(credentials: {username: string; password: string}): Observable<LoginResponse> {
    if (credentials.username === 'admin' && credentials.password === '123') {
      const fakeResponse: LoginResponse = {
        success: true,
        message: 'Login successful (test mode)',
        accessToken: 'dummy-token-123',
        data: {
          accessToken: 'dummy-token-123',
          access_token: '',
          token: '',
          user: {
            username: 'user',
            email: 'user@example.com'
          }
        }
      };

      this.saveToken(fakeResponse.accessToken || '');
      this.isAuthenticatedSubject.next(true);

      return of(fakeResponse);
    } else {
    return throwError(() => ({message: 'Invalid username or password (test mode)'}));
    }
  }

  logoutTest() {
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  registerTest(data: {username: string; email: string; password: string}): Observable<any> {
    if (data.username === "admin" && data.password === "123") {
      return of({ success: true, message: 'Registration successful (test mode)' });
    } else {
      return throwError(() => ({message: 'Registration failed (test mode)'}));
    }
  }
}
