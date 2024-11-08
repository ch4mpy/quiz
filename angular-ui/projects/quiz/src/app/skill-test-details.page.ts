import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import {
  QuizDto,
  QuizzesApi,
  SkillTestApi,
  SkillTestResultDetailsDto,
} from '@c4-soft/quiz-api';
import { ErrorDialog } from './error.dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { ToolbarComponent } from './toolbar.component';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-skill-test-details',
  imports: [
    CommonModule,
    MatCheckboxModule,
    MatIconModule,
    MatProgressBarModule,
    ToolbarComponent,
  ],
  template: `<app-toolbar [title]="title"></app-toolbar>
    <div class="page-body">
      <div style="height: 1em;">
        <mat-progress-bar
          *ngIf="isQuizzesLoading || isSkillTestsLoading"
          mode="indeterminate"
        ></mat-progress-bar>
      </div>
      <h2>
        <span>{{ skillTest?.traineeUsername }}</span>
        <span>: {{ skillTest?.score }}</span></h2>
      <h3>{{ skillTest?.traineeFirstName }} {{ skillTest?.traineeLastName }} <a [href]="email" target="_blank">{{ skillTest?.traineeEmail }}</a></h3>
      <div *ngFor="let question of quiz?.questions" style="margin-bottom: 1em;">
        <div>
          <h2>{{ question.label }}</h2>
          <div *ngFor="let choice of question.choices" style="display: block;">
            <mat-checkbox style="margin-left: 2em;"
              [checked]="isChoiceSelected(question.questionId, choice.choiceId)"
            ></mat-checkbox>
            <span >{{ choice.label }}</span>
            <mat-icon
              *ngIf="
                (isChoiceSelected(question.questionId, choice.choiceId) &&
                  choice.isGood) ||
                (!isChoiceSelected(question.questionId, choice.choiceId) &&
                  !choice.isGood)
              "
              color="primary"
              >done</mat-icon
            >
            <mat-icon
              *ngIf="
                (!isChoiceSelected(question.questionId, choice.choiceId) &&
                  choice.isGood) ||
                (isChoiceSelected(question.questionId, choice.choiceId) &&
                  !choice.isGood)
              "
              color="warn"
              >close</mat-icon
            >
          </div>
          <div>
            <p>{{ question.comment }}</p>
          </div>
        </div>
      </div>
    </div>`,
  styles: [],
})
export class SkillTestDetailsPage implements OnInit {
  isQuizzesLoading: boolean = false
  isSkillTestsLoading: boolean = false
  quiz?: QuizDto;
  skillTest?: SkillTestResultDetailsDto;

  constructor(
    private quizzesApi: QuizzesApi,
    private skillTestApi: SkillTestApi,
    private activatedRoute: ActivatedRoute,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((parameter) => {
      this.isQuizzesLoading = true;
      this.quizzesApi.getQuiz(parameter['quizId']).subscribe({
        next: (dto) => {
          this.quiz = dto;
          this.isQuizzesLoading = false;
        },
        error: (error) => {
          this.isQuizzesLoading = false;
          this.dialog.open(ErrorDialog, { data: { error } });
        },
      });

      this.isSkillTestsLoading = true
      this.skillTestApi
        .getSkillTest(parameter['quizId'], parameter['traineeName'])
        .subscribe({
          next: (dto) => {
            this.skillTest = dto;
            this.isSkillTestsLoading = false
          },
          error: (error) => {
            this.isSkillTestsLoading = false
            this.dialog.open(ErrorDialog, { data: { error } });
          },
        });
    });
  }

  get title(): string {
    return this.quiz?.title || '';
  }

  get email(): string {
    return `mailto:${this.skillTest?.traineeEmail}`
  }

  isChoiceSelected(questionId: number, choiceId: number): boolean {
    return (
      this.skillTest?.test.questions
        .filter((q) => q.questionId === questionId)
        .at(0)
        ?.choices?.filter((c) => c === choiceId)
        .at(0) !== undefined || false
    );
  }
}
