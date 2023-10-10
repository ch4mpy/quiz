import { Component, Input } from '@angular/core';
import { UserService } from './user.service';
import { BFFApi } from '@c4-soft/bff-api';
import { MatDialog } from '@angular/material/dialog';
import { QuizCreationDialog } from './quiz-creation.dialog';
import { Router } from '@angular/router';

@Component({
  selector: 'app-toolbar',
  template: `<mat-toolbar color="primary">
  <button [matMenuTriggerFor]="mainMenu" mat-icon-button aria-label="Toolbar menu button">
    <mat-icon>menu</mat-icon>
  </button>
  <span>Quiz by ch4mpy</span>
  <span class="spacer"></span>
  <span>{{ title }}</span>
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
  <button mat-menu-item [routerLink]="['/', 'quizzes']">Quizzes</button>
  <button mat-menu-item *ngIf="currentUser.isTrainer" [routerLink]="['/', 'tests']">Trainees tests</button>
  <button mat-menu-item [routerLink]="['/', 'privacy']">Privacy Policy</button>
  <a mat-menu-item href="https://oidc.c4-soft.com/auth/realms/quiz/account/">account</a>
</mat-menu>`,
  styles: [
  ]
})
export class ToolbarComponent {

  @Input()
  title!: string

  constructor(private user: UserService, private bff: BFFApi, private dialog: MatDialog, private router: Router) {}

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
