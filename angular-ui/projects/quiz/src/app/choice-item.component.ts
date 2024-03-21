import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ChoiceDto, ChoiceUpdateDto, QuizzesApi } from '@c4-soft/quiz-api';
import { Observable, Subscription, debounceTime } from 'rxjs';
import { ConfirmationDialog } from './confirmation.dialog';
import { ErrorDialog } from './error.dialog';

export interface ChoiceEvent {
  choiceId: number;
  isSelected: boolean;
}

@Component({
  selector: 'app-choice-item',
  template: `<form [formGroup]="form" style="height: 4em;">
      <mat-form-field style="width: 75%;">
        <mat-label>Label</mat-label>
        <input matInput formControlName="label" />
      </mat-form-field>
      <mat-checkbox formControlName="isGood"></mat-checkbox>
      <mat-icon
        *ngIf="(isValidated$ | async) && isCorrect && isPerQuestionResult"
        color="primary"
        >done</mat-icon
      >
      <mat-icon
        *ngIf="(isValidated$ | async) && !isCorrect && isPerQuestionResult"
        color="warn"
        >close</mat-icon
      >
    </form>
    <button
      *ngIf="isInEditMode$ | async"
      matListItemMeta
      mat-mini-fab
      color="warn"
      aria-label="Delete choice"
      matTooltip="Delete choice"
      class="item-mini-button"
      (click)="delete()"
    >
      <mat-icon>delete</mat-icon>
    </button>`,
  styles: [],
})
export class ChoiceItemComponent implements OnInit, OnDestroy {
  @Input()
  choice!: ChoiceDto;

  @Input()
  isInEditMode$!: Observable<boolean>;

  @Input()
  isValidated$!: Observable<boolean>;

  @Input()
  questionAnswer!: Map<number, boolean>;

  @Input()
  isPerQuestionResult!: boolean;

  @Output()
  readonly onDeleted = new EventEmitter<void>();

  private sub!: Subscription;

  form!: FormGroup;

  constructor(private quizApi: QuizzesApi, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.form = new FormGroup({
      label: new FormControl<string>(this.choice?.label, [Validators.required]),
      isGood: new FormControl<boolean>(false),
    });
    this.isInEditMode$.subscribe((isInEditMode) => {
      if (isInEditMode) {
        this.form.controls['isGood'].patchValue(this.choice?.isGood);
      }
      this.sub?.unsubscribe();
      if (isInEditMode) {
        this.sub = this.form.valueChanges
          .pipe(debounceTime(750))
          .subscribe((formValue) => {
            if (this.form.valid) {
              const dto: ChoiceUpdateDto = {
                label: formValue.label || '',
                isGood: formValue.isGood || false,
              };
              if (this.choice.choiceId > -1) {
                this.quizApi
                  .updateChoice(
                    this.choice.quizId,
                    this.choice.questionId,
                    this.choice.choiceId,
                    dto
                  )
                  .subscribe({
                    error: (error) => {
                      this.dialog.open(ErrorDialog, { data: { error } });
                    },
                  });
              } else {
                this.quizApi
                  .addChoice(
                    this.choice.quizId,
                    this.choice.questionId,
                    dto,
                    'response'
                  )
                  .subscribe({
                    next: (resp) => {
                      this.choice.choiceId = parseInt(
                        resp.headers.get('Location') || '-1'
                      );
                    },
                    error: (error) => {
                      this.dialog.open(ErrorDialog, { data: { error } });
                    },
                  });
              }
            }
          });
      } else {
        this.sub = this.form.valueChanges.subscribe((formValue) => {
          if (formValue.hasOwnProperty('isGood')) {
            this.questionAnswer.set(
              this.choice.choiceId,
              formValue.isGood || false
            );
          }
        });
      }
    });

    this.isValidated$.subscribe((disabled) => {
      disabled
        ? this.form.controls['isGood']?.disable()
        : this.form.controls['isGood']?.enable();
    });
  }

  get isCorrect(): boolean {
    return (
      (this.choice.isGood && this.questionAnswer.get(this.choice.choiceId)) ||
      (!this.choice.isGood && !this.questionAnswer.get(this.choice.choiceId))
    );
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  delete() {
    this.dialog
      .open(ConfirmationDialog, {
        data: { message: 'Delete choice permanently?' },
      })
      .afterClosed()
      .subscribe((isConfirmed) => {
        if (!isConfirmed) {
          return;
        }
        if (this.choice.choiceId < 0) {
          this.onDeleted.emit();
        } else {
          this.quizApi
            .deleteChoice(
              this.choice.quizId,
              this.choice.questionId,
              this.choice.choiceId
            )
            .subscribe({
              complete: () => {
                this.onDeleted.emit();
              },
              error: (error) => {
                this.dialog.open(ErrorDialog, { data: { error } });
              },
            });
        }
      });
  }
}
