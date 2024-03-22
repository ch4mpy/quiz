import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { QuizzesApi } from '@c4-soft/quiz-api';
import { ErrorDialog } from './error.dialog';
import { UserService } from './user.service';

@Component({
  selector: 'app-quiz-creation',
  template: `<mat-toolbar>
      <span>New Quiz</span>
    </mat-toolbar>
    <div style="margin: 1em;">
      <form
        [formGroup]="creationForm"
        (ngSubmit)="createQuiz()"
        id="creationForm"
      >
        <mat-form-field>
          <mat-label>Title</mat-label>
          <input matInput type="text" formControlName="titleInput" />
        </mat-form-field>
        <div>
          <mat-checkbox
            formControlName="isChoicesShuffled"
            matTooltip="Each time a question is displayed, choices are shuffled"
            >Shuffle choices</mat-checkbox
          >
        </div>
        <div>
          <mat-checkbox
            formControlName="isNotPerQuestionResult"
            matTooltip="If left unchecked, the correct answer for a question is displayed as soon as the choices for this question are validated"
            >Display answers after test is submitted</mat-checkbox
          >
        </div>
        <div>
          <mat-checkbox
            formControlName="isReplayDisabled"
            matTooltip="If checked, trainees are not allowed to submit new skill test until you deleted the old one"
            >Allow only one answer</mat-checkbox
          >
        </div>
        <div>
          <mat-checkbox
            formControlName="isTrainerNotifiedOfNewTests"
            matTooltip="If checked, trainer receives an email each time a skill test is submitted for this quiz"
            >Skill-tests notifications by email</mat-checkbox
          >
        </div>
        <button
          mat-fab
          color="primary"
          aria-label="Create new quiz"
          type="submit"
          [disabled]="creationForm.invalid"
        >
          <mat-icon>add</mat-icon>
        </button>
      </form>
    </div>`,
  styles: [],
})
export class QuizCreationDialog {
  creationForm = new FormGroup({
    titleInput: new FormControl<string>('', [Validators.required]),
    isChoicesShuffled: new FormControl<boolean>(true, []),
    isNotPerQuestionResult: new FormControl<boolean>(false, []),
    isReplayDisabled: new FormControl<boolean>(false, []),
    isTrainerNotifiedOfNewTests: new FormControl<boolean>(false, []),
  });

  constructor(
    private dialogRef: MatDialogRef<QuizCreationDialog>,
    private quizApi: QuizzesApi,
    private dialog: MatDialog,
    private user: UserService
  ) {}

  createQuiz() {
    if (this.creationForm.invalid || !this.user.current.isAuthenticated) {
      return;
    }
    this.quizApi
      .createQuiz(
        {
          title: this.creationForm.controls.titleInput.value || '',
          isChoicesShuffled:
            !!this.creationForm.controls.isChoicesShuffled.value,
          isPerQuestionResult:
            !this.creationForm.controls.isNotPerQuestionResult.value,
          isReplayEnabled: !this.creationForm.controls.isReplayDisabled.value,
          isTrainerNotifiedOfNewTests:
            !!this.creationForm.controls.isTrainerNotifiedOfNewTests.value,
        },
        'response'
      )
      .subscribe({
        next: (resp) => {
          this.dialogRef.close(resp.headers.get('Location'));
        },
        error: (error) => {
          this.dialogRef.close(null);
          this.dialog.open(ErrorDialog, { data: { error } });
        },
      });
  }
}
