import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-quiz-rejection',
  template: `
    <h2 mat-dialog-title>You are about to reject a quiz draft</h2>
    <form [formGroup]="rejectionForm">
      <mat-form-field>
        <mat-label>Why do you reject?</mat-label>
        <input matInput formControlName="message" />
      </mat-form-field>
    </form>
    <mat-dialog-actions align="end">
      <button mat-button cdkFocusInitial (click)="cancel()">Cancel</button>
      <button mat-button [disabled]="!rejectionForm.valid" (click)="reject()">
        Reject
      </button>
    </mat-dialog-actions>
  `,
  styles: [],
})
export class QuizRejectionDialog {
  rejectionForm = new FormGroup({
    message: new FormControl('', [
      Validators.required,
      Validators.minLength(3),
    ]),
  });

  constructor(private dialog: MatDialogRef<QuizRejectionDialog>) {}

  cancel() {
    this.dialog.close();
  }

  reject() {
    if (this.rejectionForm.valid) {
      this.dialog.close(this.rejectionForm.controls.message.value);
    }
  }
}
