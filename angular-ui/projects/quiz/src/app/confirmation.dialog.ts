import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatToolbarModule } from '@angular/material/toolbar';

export interface ConfirmationDialogData {
  message: string;
}

@Component({
  standalone: true,
  selector: 'app-confirmation',
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatToolbarModule,
  ],
  template: `<mat-toolbar color="primary">
      <span>Confirmation</span>
    </mat-toolbar>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button cdkFocusInitial (click)="cancel()">No</button>
      <button mat-button (click)="confirm()">Yes</button>
    </mat-dialog-actions> `,
  styles: [],
})
export class ConfirmationDialog {
  constructor(
    private dialog: MatDialogRef<ConfirmationDialog>,
    @Inject(MAT_DIALOG_DATA) readonly data: ConfirmationDialogData
  ) {}

  cancel() {
    this.dialog.close(false);
  }

  confirm() {
    this.dialog.close(true);
  }
}
