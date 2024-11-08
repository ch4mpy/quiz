import { Component, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import {
  QuizDto,
  QuizzesApi,
  SkillTestApi,
  SkillTestResultPreviewDto,
} from '@c4-soft/quiz-api';
import moment from 'moment';
import { ConfirmationDialog } from './confirmation.dialog';
import { ErrorDialog } from './error.dialog';
import { ToolbarComponent } from './toolbar.component';
import { UserService } from './user.service';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-skill-test-selection',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatProgressBarModule,
    MatSelectModule,
    MatDatepickerModule,
    ToolbarComponent,
  ],
  template: `<app-toolbar title="Skill Tests"></app-toolbar>
    <div class="page-body">
      <div style="height: 1em;">
        <mat-progress-bar
          *ngIf="isLoading"
          mode="indeterminate"
        ></mat-progress-bar>
      </div>
      <div>
        <div style="display: flex;">
          <div class="spacer"></div>
          <div [formGroup]="skillTestFilterForm">
            <mat-form-field style="margin: 1em;">
              <mat-label>Quiz</mat-label>
              <mat-select formControlName="quizSelection" name="quiz">
                <mat-option *ngFor="let quiz of quizzes" [value]="quiz.id">
                  {{ quiz.title }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field style="margin: 1em;">
              <mat-label>Skill tests date range</mat-label>
              <mat-date-range-input [rangePicker]="rangePicker">
                <input
                  matStartDate
                  formControlName="from"
                  [max]="now"
                  placeholder="From"
                />
                <input
                  matEndDate
                  formControlName="to"
                  [max]="now"
                  placeholder="To"
                />
              </mat-date-range-input>
              <mat-hint>MM/DD/YYYY – MM/DD/YYYY</mat-hint>
              <mat-datepicker-toggle
                matIconSuffix
                [for]="rangePicker"
              ></mat-datepicker-toggle>
              <mat-date-range-picker #rangePicker>
                <mat-date-range-picker-actions>
                  <button mat-button matDateRangePickerCancel>Cancel</button>
                  <button
                    mat-raised-button
                    color="primary"
                    matDateRangePickerApply
                  >
                    Apply
                  </button>
                </mat-date-range-picker-actions>
              </mat-date-range-picker>
            </mat-form-field>
          </div>

          <div class="spacer"></div>
        </div>
        <mat-list>
          <mat-list-item
            matListItemLine
            *ngFor="let st of skillTests"
            style="width: 100%; margin: 1em;"
          >
            <button
              mat-flat-button
              style="width: 70%;"
              (click)="openDetails(st.traineeName)"
            >
              {{ st.traineeName }}: {{ st.score }}
            </button>
            <button
              mat-mini-fab
              color="warn"
              (click)="deleteSkillTest(st.traineeName)"
              style="margin-left: 1em;"
            >
              <mat-icon>delete</mat-icon>
            </button>
          </mat-list-item>
        </mat-list>
      </div>
    </div>`,
  styles: [],
})
export class SkillTestSelectionPage implements OnInit {
  isLoading: boolean = false;
  now: Date = new Date();
  aWeekAgo: Date = new Date(Date.now() - 7 * 24 * 3600 * 1000);
  skillTestFilterForm = new FormGroup({
    quizSelection: new FormControl<number | null>(null, [Validators.required]),
    from: new FormControl(moment(this.aWeekAgo.toUTCString())),
    to: new FormControl(moment(this.now.toUTCString())),
  });

  quizzes: QuizDto[] = [];
  skillTests: SkillTestResultPreviewDto[] = [];

  constructor(
    private user: UserService,
    private quizzesApi: QuizzesApi,
    private skillTestApi: SkillTestApi,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizzesApi.getQuizList(this.user.current.name).subscribe({
      next: (dto) => {
        this.quizzes = dto;
        this.skillTestFilterForm.controls.quizSelection.patchValue(dto[0]?.id);
      },
      error: (error) => {
        this.dialog.open(ErrorDialog, { data: { error } });
      },
    });

    this.skillTestFilterForm.valueChanges.subscribe((skillTestFilter) => {
      if (
        skillTestFilter.quizSelection === null ||
        skillTestFilter.quizSelection === undefined
      ) {
        return;
      }
      this.skillTests = [];
      console.log(
        'getSkillTestList',
        skillTestFilter.quizSelection,
        skillTestFilter.from?.unix(),
        skillTestFilter.to?.unix()
      );
      this.skillTestApi
        .getSkillTestList(
          skillTestFilter.quizSelection,
          skillTestFilter.from?.unix(),
          skillTestFilter.to?.unix()
        )
        .subscribe({
          next: (dto) => {
            this.skillTests = dto;
          },
          error: (error) => {
            this.dialog.open(ErrorDialog, { data: { error } });
          },
        });
    });
  }

  openDetails(traineeName: string) {
    this.router.navigate([
      '/',
      'tests',
      this.skillTestFilterForm.controls.quizSelection.value,
      traineeName,
    ]);
  }

  deleteSkillTest(traineeName: string) {
    if (!this.user.current.isAuthenticated) {
      return;
    }
    this.dialog
      .open(ConfirmationDialog, {
        data: { message: 'Delete skill-test permanently?' },
      })
      .afterClosed()
      .subscribe({
        next: (isConfirmed) => {
          if (!isConfirmed) {
            return;
          }

          this.isLoading = true;
          this.skillTestApi
            .deleteSkillTest(
              this.skillTestFilterForm.controls.quizSelection.value || -1,
              traineeName
            )
            .subscribe({
              complete: () => {
                this.isLoading = false;
                const idx = this.skillTests.findIndex(
                  (st) => st.traineeName === traineeName
                );
                if (idx >= 0) {
                  this.skillTests.splice(idx, 1);
                }
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
