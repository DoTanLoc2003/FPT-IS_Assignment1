import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { inject } from '@angular/core';

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

  logOut() {
    alert('Logging out, navigating to Home')
    this.router.navigate(['/home']);
  }

}
