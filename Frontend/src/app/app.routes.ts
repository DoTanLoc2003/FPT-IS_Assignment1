import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { authGuard } from './services/auth.guard';
import { guestGuard } from './services/guest.guard';

export const routes: Routes = [

    {path: 'home', title: 'Home', component: HomeComponent},
    {path: 'login', title: 'Login', component: LoginComponent, canActivate: [guestGuard]},
    {path: 'register', title: 'Register', component: RegisterComponent, canActivate: [guestGuard]},
    {path: 'profile', title: 'Profile', component: ProfileComponent, canActivate: [authGuard], data: {roles: ['user']}},
    {path: '', redirectTo: '/home', pathMatch: 'full'},
    {path: '**', redirectTo: '/home'},
];
