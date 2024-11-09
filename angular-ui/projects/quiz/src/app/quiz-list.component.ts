import { CommonModule } from '@angular/common';
import { Component, input, output, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
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
import {
  QuizzesFilterCriteria,
  QuizzesFilterFormComponent,
} from './quizzes-filter-form.component';
import { ToolbarComponent } from './toolbar.component';
import { UserService } from './user.service';

@Component({
  standalone: true,
  selector: 'app-quiz-list',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
  ],
  template: `<mat-list>
        <mat-list-item
          *ngFor="let quiz of value()"
          style="width: 100%;"
        >
          <span matListItemLine style="display: inline-flex; width: 100%;">
            <button
              mat-flat-button
              (click)="
                selectQuiz.emit(quiz)
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
              (click)="deleteQuiz.emit(quiz)"
              matListItemMeta
              mat-mini-fab
              color="warn"
              aria-label="Delete quiz"
              matTooltip="Delete quiz"
              class="item-mini-button"
            >
              <mat-icon>delete</mat-icon>
            </button>
            <button
              *ngIf="canDelete(quiz)"
              (click)="downloadQuiz(quiz)"
              color="primary"
              matListItemMeta
              mat-mini-fab
              aria-label="Download quiz as JSON file"
              matTooltip="Download as JSON"
              class="item-mini-button"
            >
              <mat-icon>download</mat-icon>
            </button>
          </span>
        </mat-list-item>
      </mat-list>`,
  styles: [],
})
export class QuizListComponent {
  readonly value = input<QuizDto[]>([]);
  readonly selectQuiz = output<QuizDto>();
  readonly deleteQuiz = output<QuizDto>();

  constructor(
    private user: UserService,
  ) {}

  get isAuthor(): boolean {
    return this.user.current.isTrainer;
  }

  canDelete(quiz: QuizDto): boolean {
    return (
      this.user.current.isModerator ||
      quiz?.authorName === this.user.current.name
    );
  }

  protected downloadQuiz(quiz: QuizDto) {
    const copy = { ...quiz } as any;
    delete copy.id;
    const blob = this.toJsonBlob(copy);
    const linkElement = document.createElement('a');
    linkElement.href = URL.createObjectURL(blob);
    linkElement.download = `${quiz?.title || 'quiz'}.json`;
    linkElement.click();
    window.URL.revokeObjectURL(linkElement.href);
  }

  private toJsonBlob(data: unknown): Blob {
    return new Blob([JSON.stringify(data)], {
      type: 'application/json',
    });
  }
}
