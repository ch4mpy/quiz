import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { QuizDto, QuizzesApi } from '@c4-soft/quiz-api';
import { QuizCreationDialog } from './quiz-creation.dialog';
import { ConfirmationDialog } from './confirmation.dialog';
import { UserService } from './user.service';
import { ErrorDialog } from './error.dialog';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-quiz-selection',
  template: `<app-toolbar title="Quizzes"></app-toolbar>
    <div class="page-body">
      <div style="display: flex;">
        <div class="spacer"></div>
        <div [formGroup]="quizFilterForm">
          <mat-form-field style="margin: 1em;">
            <mat-label>Title</mat-label>
            <input matInput formControlName="title" />
          </mat-form-field>
          <mat-form-field style="margin: 1em;">
            <mat-label>Author</mat-label>
            <input matInput formControlName="author" />
          </mat-form-field>
        </div>
        <div class="spacer"></div>
        <button
          *ngIf="isAuthor"
          (click)="openQuizCreationDialog()"
          mat-fab
          matTooltip="Create a quiz"
          color="primary"
          aria-label="create quiz"
          class="item-button"
        >
          <mat-icon>add</mat-icon>
        </button>
      </div>
      <div style="height: 1em;">
        <mat-progress-bar
          *ngIf="isLoading"
          mode="indeterminate"
        ></mat-progress-bar>
      </div>
      <mat-list>
        <mat-list-item *ngFor="let quiz of quizzes" style="width: 100%;">
          <span matListItemLine style="display: inline-flex; width: 100%;">
            <button
              mat-flat-button
              (click)="
                openQuizDetails(quiz.id, !quiz.isPublished && !quiz.isReplaced)
              "
              style="display: block; width: 90%; margin: 1em;"
            >
              <span style="display: inline-flex; width: 100%;">
                <span>{{ quiz.title }}</span>
                <span class="spacer"></span>

                <span>{{ quiz.authorName }}</span>
                <span style="width: 2em;">
                  <mat-icon
                    *ngIf="
                      !quiz.isPublished && !quiz.isSubmitted && !quiz.isReplaced
                    "
                    matTooltip="Draft"
                    >pending</mat-icon
                  >
                  <mat-icon
                    *ngIf="
                      !quiz.isPublished && !quiz.isSubmitted && quiz.isReplaced
                    "
                    matTooltip="Repalced"
                    >visibility_off</mat-icon
                  >
                  <mat-icon
                    *ngIf="!quiz.isPublished && quiz.isSubmitted"
                    matTooltip="Waiting for moderation"
                    >hourglass_empty</mat-icon
                  >
                </span>
                <span matListItemMeta style="width: 2em;">{{
                  quiz.questions.length
                }}</span>
              </span>
            </button>
            <button
              *ngIf="canDelete(quiz)"
              (click)="deleteQuiz(quiz.id)"
              matListItemMeta
              mat-mini-fab
              color="warn"
              aria-label="Delete quiz"
              matTooltip="Delete quiz"
              class="item-mini-button"
            >
              <mat-icon>delete</mat-icon>
            </button>
          </span>
        </mat-list-item>
      </mat-list>
    </div>`,
  styles: [],
})
export class QuizSelectionPage implements OnInit {
  quizzes: QuizDto[] = [];
  isLoading = true;
  quizFilterForm = new FormGroup({
    author: new FormControl<string>(''),
    title: new FormControl<string>(''),
  });

  constructor(
    private quizApi: QuizzesApi,
    private dialog: MatDialog,
    private router: Router,
    private user: UserService
  ) {}

  ngOnInit(): void {
    this.quizFilterForm.valueChanges.subscribe(() => {
      this.loadQuizzes();
    });
    this.loadQuizzes();
  }

  get isAuthor(): boolean {
    return this.user.current.isTrainer;
  }

  private loadQuizzes() {
    this.isLoading = true;
    this.quizApi
      .getQuizList(
        this.quizFilterForm.controls.author.value || '',
        this.quizFilterForm.controls.title.value || ''
      )
      .subscribe({
        next: (quizzes) => {
          this.isLoading = false;
          this.quizzes = quizzes;
        },
        error: () => {
          this.isLoading = false;
          this.quizzes = [];
        },
      });
  }

  openQuizCreationDialog() {
    const dialogRef = this.dialog.open(QuizCreationDialog);
    dialogRef.afterClosed().subscribe((quizId) => {
      if (!!quizId) {
        this.openQuizDetails(quizId, true);
      } else {
        this.loadQuizzes();
      }
    });
  }

  openQuizDetails(quizId: number, isEditMode: boolean) {
    this.router.navigate(['quizzes', quizId], {
      queryParams: { edit: isEditMode },
    });
  }

  canDelete(quiz: QuizDto): boolean {
    return (
      this.user.current.isModerator ||
      quiz?.authorName === this.user.current.name
    );
  }

  deleteQuiz(quizId: number) {
    this.dialog
      .open(ConfirmationDialog, {
        data: { message: 'Delete quiz permanently?' },
      })
      .afterClosed()
      .subscribe({
        next: (isConfirmed) => {
          if (!isConfirmed) {
            return;
          }
          this.isLoading = true;
          this.quizApi.deleteQuiz(quizId).subscribe({
            complete: () => {
              this.isLoading = false;
              this.loadQuizzes();
            },
            error: (error) => {
              this.isLoading = false;
              this.dialog.open(ErrorDialog, { data: { error } });
            },
          });
        },
      });
  }
}
