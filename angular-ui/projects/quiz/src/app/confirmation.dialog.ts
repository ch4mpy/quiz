import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface ConfirmationDialogData {
  message: string;
}

@Component({
  selector: 'app-confirmation',
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
