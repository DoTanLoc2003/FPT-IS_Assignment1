import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <h2> Welcome to the Home Page! </h2>
    <button routerLink="/login">Go to Login</button>
    <button routerLink="/register">Go to Register</button>
    `,
})
export class HomeComponent {

}
