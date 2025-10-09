import { CanActivateChildFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { AuthService } from "./auth.service";

export const guestGuard: CanActivateChildFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if(authService.authenticated()) {
        router.navigate(['/profile']);
        return false;
    }
    return true;

}