import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { QuizDto, QuizzesApi } from '@c4-soft/quiz-api';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  mergeMap,
  Observable,
  tap,
} from 'rxjs';
import { ConfirmationDialog } from './confirmation.dialog';
import { ErrorDialog } from './error.dialog';
import { QuizCreationDialog } from './quiz-creation.dialog';
import { QuizListComponent } from './quiz-list.component';
import {
  QuizzesFilterCriteria,
  QuizzesFilterFormComponent,
} from './quizzes-filter-form.component';
import { ToolbarComponent } from './toolbar.component';
import { UserService } from './user.service';

@Component({
  standalone: true,
  selector: 'app-quiz-selection',
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    QuizzesFilterFormComponent,
    QuizListComponent,
    ToolbarComponent,
  ],
  template: `<app-toolbar title="Quizzes"></app-toolbar>
    <div class="page-body">
      <div style="display: flex;">
        <div class="spacer"></div>
        <app-quizzes-filter-form
          [value]="(searchCriteria$ | async) || {}"
          (valueChanges)="searchCriteria$.next($event)"
        />
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
        @if(isLoading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
        }
      </div>
      <app-quiz-list
        [value]="(quizzes$ | async) || []"
        (selectQuiz)="
          openQuizDetails($event.id, !$event.isPublished && !$event.isReplaced)
        "
        (deleteQuiz)="deleteQuiz($event.id)"
      />
    </div>`,
  styles: [],
})
export class QuizSelectionPage {
  protected isLoading = signal(false);

  protected readonly searchCriteria$ =
    new BehaviorSubject<QuizzesFilterCriteria>({});
  private readonly forceUpdate$ = new BehaviorSubject<void>(undefined);
  protected readonly quizzes$: Observable<QuizDto[]>;

  constructor(
    private quizApi: QuizzesApi,
    private dialog: MatDialog,
    private router: Router,
    private user: UserService
  ) {
    this.quizzes$ = combineLatest([this.searchCriteria$, this.forceUpdate$])
      .pipe(tap(() => this.isLoading.set(true)))
      .pipe(
        mergeMap(([criteria]) =>
          this.quizApi.getQuizList(criteria.author, criteria.title)
        )
      )
      .pipe(
        catchError((e) => {
          this.isLoading.set(false);
          throw e;
        })
      )
      .pipe(tap(() => this.isLoading.set(false)));
  }

  get isAuthor(): boolean {
    return this.user.current.isTrainer;
  }

  openQuizCreationDialog() {
    const dialogRef = this.dialog.open(QuizCreationDialog);
    dialogRef.afterClosed().subscribe((quizId) => {
      if (!!quizId) {
        this.openQuizDetails(quizId, true);
      } else {
        this.forceUpdate$.next();
      }
    });
  }

  openQuizDetails(quizId: number, isEditMode: boolean) {
    this.router.navigate(['quizzes', quizId], {
      queryParams: { edit: isEditMode },
    });
  }

  deleteQuiz(quizId: number) {
    if (!this.user.current.isAuthenticated) {
      return;
    }
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
          this.isLoading.set(true);
          this.quizApi.deleteQuiz(quizId).subscribe({
            complete: () => {
              this.isLoading.set(false);
              this.forceUpdate$.next();
            },
            error: (error) => {
              this.isLoading.set(false);
              this.dialog.open(ErrorDialog, { data: { error } });
            },
          });
        },
      });
  }
}
