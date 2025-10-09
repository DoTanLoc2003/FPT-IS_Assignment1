import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2> Profile Page </h2>
    <p> This should work if login test passed </p>

    <button type="submit" (click)="logOut()">Logout</button>
  `,
  styles: [`
    h2 { margin-bottom: 10px; }
    button { margin-top: 20px; }
  `]
})
export class ProfileComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  logOut() {
    this.authService.logoutTest();
    alert('Logged out successfully!');
    this.router.navigate(['/home']);
  }

}
