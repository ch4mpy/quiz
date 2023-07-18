import { Component } from '@angular/core';
import { UserService } from './user.service';
import { BFFApi } from '@c4-soft/bff-api';

@Component({
  selector: 'app-toolbar',
  template: `<mat-toolbar color="primary">
  <button [matMenuTriggerFor]="mainMenu" mat-icon-button aria-label="Toolbar menu button">
    <mat-icon>menu</mat-icon>
  </button>
  <span>Quiz by ch4mpy</span>
  <span class="spacer"></span>
  <button
    *ngIf="!currentUser.isAuthenticated"
    mat-icon-button
    aria-label="Login button"
    (click)="login()"
  >
    <mat-icon>login</mat-icon>
  </button>
  <span *ngIf="currentUser.isAuthenticated">
    <span>{{ currentUser.name }}</span>
    <button mat-icon-button aria-label="Logout button" (click)="logout()">
      <mat-icon>logout</mat-icon>
    </button>
  </span>
</mat-toolbar>
<mat-menu #mainMenu="matMenu">
  <button *ngIf="currentUser.hasAnyRole('former', 'moderator')" mat-menu-item>Create Quiz</button>
  <button mat-menu-item>Skill test</button>
</mat-menu>`,
  styles: [
  ]
})
export class ToolbarComponent {
  constructor(private user: UserService, private bff: BFFApi) {}

  get currentUser() {
    return this.user.current;
  }

  login() {
    this.bff.getLoginOptions().subscribe((opts) => {
      if (opts.length > 0) {
        this.user.login(opts[0].loginUri);
      }
    });
  }

  logout() {
    this.user.logout();
  }
}
