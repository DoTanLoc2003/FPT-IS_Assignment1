import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { inject } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <h1> Login Inline template </h1>
    <form (ngSubmit)="onSubmit()" #loginForm="ngForm">
      <label for="username">Username:</label>
      <input type="text" id="username" name="username" [(ngModel)]="username" required>

      <label for="password">Password:</label>
      <input type="password" id="password" name="password" [(ngModel)]="password" required>

      <button type="submit" [disabled]="!loginForm.form.valid">Login</button>
    </form>
  `,
  styles: [`
    form {
      display: flex;
      flex-direction: column;
      max-width: 250px;
    }
    label {
      margin-top: 10px;
    }
    button {
      margin-top: 15px;
    }
  `]
})
export class LoginComponent {
  username: string = '';
  password: string = '';

  private router = inject(Router);
  private authService = inject(AuthService);

  onSubmit() {
    const credentials = {
      username: this.username,
      password: this.password
    };

    this.authService.loginTest(credentials).subscribe({
      next : () => {
        alert('Login successfully!!');
        this.router.navigate(['/profile']);
      },
      error: (err) => {
        alert('Login failed: ' + err.message);
      }
    });
  }
}
