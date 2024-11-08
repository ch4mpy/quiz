import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ToolbarComponent } from './toolbar.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { CommonModule } from '@angular/common';

export interface ErrorDialogData {
  error: any;
}

@Component({
  standalone: true,
  selector: 'app-error',
  imports: [
    CommonModule,
    MatToolbarModule,
    ToolbarComponent,
  ],
  template: `<mat-toolbar color="warn">
      <span>{{ title }}</span>
    </mat-toolbar>
    <div style="margin: 3em;">
      {{ message }}
    </div>`,
  styles: [],
})
export class ErrorDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: ErrorDialogData) {}

  get title() {
    var title = 'Error';
    if (this.data.error.hasOwnProperty('status')) {
      title = `${title}: ${this.data.error['status']}`;
      if (this.data.error.hasOwnProperty('statusText')) {
        title = `${title} ${this.data.error['statusText']}`;
      }
    }
    return title;
  }

  get message() {
    if (this.data.error.hasOwnProperty('message')) {
      return this.data.error['message'];
    }
    return this.data.error;
  }
}
