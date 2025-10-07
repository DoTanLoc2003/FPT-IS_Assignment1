import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <nav>
      <a routerLink="/home">Home</a> |
      <a routerLink="/login">Login</a> |
      <a routerLink="/register">Register</a> |
      <a routerLink="/profile">Profile</a>
    </nav>

    <hr/>
    <router-outlet></router-outlet>
  `,
})
export class AppComponent {
  title = 'Frontend';
}
