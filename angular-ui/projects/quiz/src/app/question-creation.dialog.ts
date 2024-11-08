import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from '@angular/material/dialog';
import { QuizzesApi } from '@c4-soft/quiz-api';
import { ErrorDialog } from './error.dialog';
import { UserService } from './user.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { CommonModule } from '@angular/common';

export interface QuestionCreationDialogData {
  quizId: number;
}

@Component({
  standalone: true,
  selector: 'app-question-creation',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatToolbarModule,
  ],
  template: `<mat-toolbar>
      <span>New Question</span>
    </mat-toolbar>
    <form [formGroup]="creationForm" (ngSubmit)="createQuestion()">
      <mat-form-field>
        <mat-label>Label</mat-label>
        <input
          matInput
          type="text"
          formControlName="labelInput"
          (keyup.enter)="createQuestion()"
        />
      </mat-form-field>
      <mat-form-field>
        <mat-label>Comment</mat-label>
        <input matInput type="text" formControlName="commentInput" />
      </mat-form-field>
      <button
        mat-fab
        color="primary"
        aria-label="Create new question"
        type="submit"
        [disabled]="creationForm.invalid"
      >
        <mat-icon>add</mat-icon>
      </button>
    </form>`,
  styles: [],
})
export class QuestionCreationDialog {
  creationForm = new FormGroup({
    labelInput: new FormControl('', [Validators.required]),
    commentInput: new FormControl('', []),
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: QuestionCreationDialogData,
    private dialogRef: MatDialogRef<QuestionCreationDialog>,
    private quizApi: QuizzesApi,
    private dialog: MatDialog,
    private user: UserService
  ) {}

  createQuestion() {
    if (this.creationForm.invalid || !this.user.current.isAuthenticated) {
      return;
    }
    this.quizApi
      .addQuestion(
        this.data.quizId,
        {
          label: this.creationForm.controls.labelInput.value || '',
          formattedBody: '',
          comment: this.creationForm.controls.commentInput.value || '',
        },
        'response'
      )
      .subscribe({
        next: (resp) => {
          var questionId = resp.headers.get('Location');
          this.dialogRef.close(questionId ? +questionId : null);
        },
        error: (error) => {
          this.dialogRef.close(null);
          this.dialog.open(ErrorDialog, { data: { error } });
        },
      });
  }
}
