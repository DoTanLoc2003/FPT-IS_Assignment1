import { HttpInterceptorFn, HttpErrorResponse } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, throwError } from "rxjs";
import { AuthService } from "./auth.service";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    
    if (req.url.includes('/login') || req.url.includes('/register')) {
        return next(req);
    }

    alert('Getting token)');
    const token = authService.getToken();

    if(token) {
        alert('Token found');
        req = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    alert('Handling request without token');

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            if(error.status === 401) {
                alert('Unauthorized request - logging out');
                authService.logout();
            } else {
                alert('Unexpected error occurred');
            }
            return throwError(() => error);
        })
    );
};