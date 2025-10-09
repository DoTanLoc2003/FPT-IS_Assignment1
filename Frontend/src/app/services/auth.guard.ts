import { inject } from "@angular/core";
import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import { AuthService } from "./auth.service";
import { map, take } from "rxjs/operators";

export const authGuard: CanActivateFn = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.isAuthenticated$.pipe(
        take(1),
        map(isAuthenticated => {
            if (!isAuthenticated) {
                alert('Not authenticated - redirecting to login.');
                router.navigate(['/login']);
                return false;
            }

            const expectedRole = route.data?.['roles'] as string[] | undefined;
            const userRole = authService.getUserRole() || '';

            if (expectedRole && !expectedRole.includes(userRole)) {
                alert('Access denided - You do not have permission to access this page.');
                router.navigate(['/profile']);
                return false;
            }
            return true;
        })
    );
}
