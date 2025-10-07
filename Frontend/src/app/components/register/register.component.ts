import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { inject } from '@angular/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <h2>Register Page</h2>

    <form (ngSubmit)="onSubmit()" #registerForm="ngForm">
      <label for="username">Username:</label>
      <input type="text" id="username" name="username" [(ngModel)]="username" required />

      <label for="email">Email:</label>
      <input type="email" id="email" name="email" [(ngModel)]="email" required />

      <label for="password">Password:</label>
      <input type="password" id="password" name="password" [(ngModel)]="password" required /> 

      <button type="submit" [disabled]="!registerForm.form.valid" (click)="onSubmit()">Register</button>
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
export class RegisterComponent {
  username: string ='';
  email: string ='';
  password: string='';

  private router = inject(Router);

  onSubmit() {
    const registerData = {
      username: this.username,
      email: this.email,
      password: this.password
    };

    if (this.username === "admin" && this.password === "123") {
      alert('Test admin: registration successful!');
      this.router.navigate(['/login']);
    } else {
      alert('Failed to do register test. Stay here!!');
    }
  };
}
