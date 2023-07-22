import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionPanel } from '@angular/material/expansion';
import {
  ChoiceDto,
  QuestionDto,
  QuizzesApi,
  SkillTestDto,
} from '@c4-soft/quiz-api';
import { BehaviorSubject, Observable, debounceTime } from 'rxjs';
import { ConfirmationDialog } from './confirmation.dialog';
import { ErrorDialog } from './error.dialog';

@Component({
  selector: 'app-question-expansion-pannel',
  template: ` <mat-expansion-panel
    [expanded]="expanded"
    (opened)="opened.emit()"
    cdkDrag
    [cdkDragDisabled]="!isDragable"
    style="width: 100%;"
  >
    <mat-expansion-panel-header>
      {{ question.label }}
      <span class="spacer"></span>
      <mat-icon
        *ngIf="(isValidated$ | async) && isAnswerCorrect && isPerQuestionResult"
        color="primary"
        >done</mat-icon
      >
      <mat-icon
        *ngIf="
          (isValidated$ | async) && !isAnswerCorrect && isPerQuestionResult
        "
        color="warn"
        >close</mat-icon
      >
    </mat-expansion-panel-header>
    <mat-form-field *ngIf="isInEditMode$ | async" style="width: 100%">
      <mat-label>Question</mat-label>
      <input
        matInput
        [formControl]="labelCtrl"
        style="display: block; width: 100%"
      />
    </mat-form-field>
    <mat-list #choices>
      <mat-list-item
        *ngFor="let choice of question.choices"
        style="margin: .5em;"
      >
        <app-choice-item
          [isInEditMode$]="isInEditMode$"
          [choice]="choice"
          [isValidated$]="isValidated$"
          [questionAnswer]="questionAnswer"
          [isPerQuestionResult]="isPerQuestionResult"
          (onDeleted)="choiceDeleted(choice)"
        ></app-choice-item>
      </mat-list-item>
      <div style="width: 100%; display:inline-flex;">
        <span class="spacer"></span>
        <button
          *ngIf="isInEditMode$ | async"
          mat-mini-fab
          color="primary"
          class="item-mini-button"
          aria-label="Add a choice"
          matTooltip="Add a choice"
          (click)="addChoice()"
        >
          <mat-icon>add</mat-icon>
        </button>
        <button
          *ngIf="isInEditMode$ | async"
          mat-mini-fab
          color="warn"
          class="item-mini-button"
          aria-label="Delete question"
          matTooltip="Delete question"
          (click)="delete()"
        >
          <mat-icon>delete</mat-icon>
        </button>
      </div>
    </mat-list>
    <div style="width: 100%; display:inline-flex;">
      <mat-form-field *ngIf="isInEditMode$ | async" style="width: 100%">
        <mat-label>Comment</mat-label>
        <textarea
          matInput
          [formControl]="commentCtrl"
          style="display: block; width: 100%"
        ></textarea>
      </mat-form-field>
      <p
        *ngIf="
          (isValidated$ | async) &&
          !(isInEditMode$ | async) &&
          isPerQuestionResult
        "
      >
        {{ question.comment }}
      </p>
      <span class="spacer"></span>
      <button
        *ngIf="!(isValidated$ | async) && !(isInEditMode$ | async)"
        (click)="validateAnswer()"
        mat-fab
        color="primary"
        class="item-button"
        aria-label="Validate answer"
        matTooltip="Validate answer"
      >
        <mat-icon>done</mat-icon>
      </button>
    </div>
  </mat-expansion-panel>`,
  styles: [],
  viewProviders: [MatExpansionPanel],
})
export class QuestionExpansionPannelComponent implements OnInit {
  @Input()
  question!: QuestionDto;

  @Input()
  expanded!: boolean;

  @Input()
  isInEditMode$!: Observable<boolean>;

  @Input()
  isDragable!: boolean;

  @Input()
  skillTest!: SkillTestDto;

  @Input()
  isChoicesShuffled!: boolean;

  @Input()
  isPerQuestionResult!: boolean;

  @Output()
  onQuestionDeleted = new EventEmitter<void>();

  @Output()
  onAnswerValidated = new EventEmitter<void>();

  @Output()
  opened = new EventEmitter<void>();

  isValidated$ = new BehaviorSubject<boolean>(false);
  isAnswerCorrect?: boolean;

  labelCtrl = new FormControl<string>('', [
    Validators.required,
    Validators.minLength(1),
  ]);
  commentCtrl = new FormControl<string>('');

  questionAnswer = new Map<number, boolean>();

  constructor(private quizApi: QuizzesApi, private dialog: MatDialog) {}

  ngOnInit() {
    if (this.isChoicesShuffled) {
      if (!this.question.choices) {
        this.question.choices = [];
      }
      this.question.choices.sort((a, b) => Math.random() - 0.5);
    }
    const answer = this.skillTest.questions.find(
      (q) => q.questionId === this.question.questionId
    ) || { questionId: this.question.questionId };
    (answer.choices || []).forEach((choiceId) => {
      this.questionAnswer.set(choiceId, true);
    });

    this.commentCtrl.patchValue(this.question.comment);
    this.labelCtrl.patchValue(this.question.label || '');
    this.commentCtrl.valueChanges
      .pipe(debounceTime(500))
      .subscribe(() => this.updateQuestion());
    this.labelCtrl.valueChanges
      .pipe(debounceTime(500))
      .subscribe(() => this.updateQuestion());
  }

  private updateQuestion() {
    if (this.labelCtrl.valid && this.commentCtrl.valid) {
      this.quizApi
        .updateQuestion(this.question.quizId, this.question.questionId, {
          label: this.labelCtrl.value || '',
          comment: this.commentCtrl.value || '',
        })
        .subscribe({
          next: () => {
            this.question.comment = this.commentCtrl.value || '';
            this.question.label = this.labelCtrl.value || '';
          },
          error: (error) => {
            this.dialog.open(ErrorDialog, { data: { error } });
          },
        });
    }
  }

  delete() {
    this.dialog
      .open(ConfirmationDialog, {
        data: { message: 'Delete question permanently?' },
      })
      .afterClosed()
      .subscribe((isConfirmed) => {
        if (!isConfirmed) {
          return;
        }
        this.quizApi
          .deleteQuestion(this.question.quizId, this.question.questionId)
          .subscribe({
            next: () => {
              this.onQuestionDeleted.emit();
            },
            error: (error) => {
              this.dialog.open(ErrorDialog, { data: { error } });
            },
          });
      });
  }

  addChoice() {
    this.question.choices = this.question.choices?.concat({
      quizId: this.question.quizId,
      questionId: this.question.questionId,
      choiceId: -1,
      label: '',
      isGood: false,
    });
  }

  choiceDeleted(choice: ChoiceDto) {
    this.question.choices = this.question.choices?.filter(
      (c) => c.choiceId !== choice.choiceId
    );
  }

  validateAnswer() {
    const goodChoices = this.question.choices?.filter((c) => !!c.isGood) || [];
    const answerChoices: number[] = [];
    this.questionAnswer.forEach((isSelected: boolean, choiceId: number) => {
      if (isSelected) {
        answerChoices.push(choiceId);
      }
    });
    this.isAnswerCorrect = answerChoices.length === goodChoices.length;
    goodChoices.forEach((c) => {
      if (!answerChoices.includes(c.choiceId)) {
        this.isAnswerCorrect = false;
      }
    });
    if (!this.skillTest.questions) {
      this.skillTest.questions = [];
    }
    const existing = this.skillTest.questions.findIndex(
      (q) => q.questionId === this.question.questionId
    );
    if (existing > -1) {
      this.skillTest.questions[existing].choices = answerChoices;
    } else {
      this.skillTest.questions.push({
        questionId: this.question.questionId,
        choices: answerChoices,
      });
    }
    this.isValidated$.next(true);
    this.onAnswerValidated.emit();
  }
}
