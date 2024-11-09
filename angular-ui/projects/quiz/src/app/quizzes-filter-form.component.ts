import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface QuizzesFilterCriteria {
  title?: string;
  author?: string;
}

@Component({
  standalone: true,
  selector: 'app-quizzes-filter-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `<div [formGroup]="quizFilterForm">
    <mat-form-field style="margin: 1em;">
      <mat-label>Title</mat-label>
      <input matInput formControlName="title" />
    </mat-form-field>
    <mat-form-field style="margin: 1em;">
      <mat-label>Author</mat-label>
      <input matInput formControlName="author" />
    </mat-form-field>
  </div>`,
  styles: [],
})
export class QuizzesFilterFormComponent {
  readonly value = input<QuizzesFilterCriteria>({});

  readonly valueChanges = output<QuizzesFilterCriteria>();

  protected readonly quizFilterForm = new FormGroup({
    title: new FormControl<string | null>(null),
    author: new FormControl<string | null>(null),
  });

  constructor() {
    toObservable(this.value)
      .pipe(takeUntilDestroyed())
      .subscribe((value) => {
        if (
          this.quizFilterForm.value.title !== value.title ||
          this.quizFilterForm.value.author !== value.author
        ) {
          this.quizFilterForm.patchValue(value);
        }
    });

    this.quizFilterForm.valueChanges.subscribe((value) => {
      if (
        this.value().title !== value.title ||
        this.value().author !== value.author
      ) {
        this.valueChanges.emit({
          title: value.title || '',
          author: value.author || '',
        });
      }
    });
  }
}
