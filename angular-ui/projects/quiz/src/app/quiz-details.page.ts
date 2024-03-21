import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import {
  QuestionDto,
  QuizDto,
  QuizzesApi,
  SkillTestApi,
  SkillTestDto,
} from '@c4-soft/quiz-api';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ErrorDialog } from './error.dialog';
import { QuestionCreationDialog } from './question-creation.dialog';
import { QuizRejectionDialog } from './quiz-rejection.dialog';
import { SkillTestResultDialog } from './skill-test-result.dialog';
import { UserService } from './user.service';

@Component({
  selector: 'app-quiz',
  template: `<app-toolbar [title]="quiz?.title || ''"></app-toolbar>
    <div class="page-body">
      <div style="height: 1em;">
        <mat-progress-bar
          *ngIf="isLoading"
          mode="indeterminate"
        ></mat-progress-bar>
      </div>
      <div style="width: 100%; display: flex;">
        <div [formGroup]="titleForm" style="width: 100%">
          <mat-form-field *ngIf="isInEditMode$ | async" style="width: 100%">
            <mat-label>Quiz title</mat-label>
            <input matInput formControlName="title" />
          </mat-form-field>
        </div>
        <div class="spacer"></div>
        <button
          *ngIf="canEditQuiz && !quiz?.isPublished && !quiz?.isSubmitted && !isModerator"
          (click)="submitDraft()"
          mat-fab
          color="primary"
          aria-label="Submit to moderation"
          matTooltip="Submit to moderation"
          class="item-button"
        >
          <mat-icon>rocket_launch</mat-icon>
        </button>
        <button
          *ngIf="canPublish && !quiz?.isPublished && (quiz?.isSubmitted || isModerator)"
          (click)="publishDraft()"
          mat-fab
          color="primary"
          aria-label="Publish"
          matTooltip="Publish"
          class="item-button"
        >
          <mat-icon>done</mat-icon>
        </button>
        <button
          *ngIf="canPublish && !quiz?.isPublished && quiz?.isSubmitted"
          (click)="rejectDraft()"
          mat-fab
          color="primary"
          aria-label="Reject"
          matTooltip="Reject"
          class="item-button"
        >
          <mat-icon>close</mat-icon>
        </button>
        <button
          *ngIf="isTrainer && !!quiz?.isPublished"
          (click)="createCopy()"
          mat-fab
          color="primary"
          aria-label="Create a copy as new draft"
          matTooltip="Create a copy as new draft"
          class="item-button"
        >
          <mat-icon>content_copy</mat-icon>
        </button>
        <button
          *ngIf="canEditQuiz && !(isInEditMode$ | async)"
          (click)="edit()"
          mat-fab
          color="primary"
          aria-label="Edit"
          matTooltip="Edit"
          class="item-button"
        >
          <mat-icon>edit</mat-icon>
        </button>
        <button
          *ngIf="!!(isInEditMode$ | async)"
          (click)="editOff()"
          mat-fab
          color="primary"
          aria-label="Quit edit mode"
          matTooltip="Quit edit mode"
          class="item-button"
        >
          <mat-icon>edit_off</mat-icon>
        </button>
      </div>
      <h2 *ngIf="!isAuthenticated" style="width: 100%;">
        Authenticate <b class="warn-color">before</b> answering the test if you
        intend to submit it.
      </h2>
      <h2
        *ngIf="isAnswerSubmitted && !quiz?.isReplayEnabled"
        style="width: 100%;"
      >
        You already provided an answer to this quiz which currently accept only
        one answer per trainee. Ask your trainer to delete your answer
        <b class="warn-color">before</b> you try to submit a new one.
      </h2>
      <div [formGroup]="quizOptionsForm" *ngIf="canEditQuiz">
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
            >Answers after test is submitted</mat-checkbox
          >
        </div>
        <div>
          <mat-checkbox
            formControlName="isReplayDisabled"
            matTooltip="If checked, trainees are not allowed to submit new skill test until you deleted the old one"
            >One answer per trainee</mat-checkbox
          >
        </div>
        <div>
          <mat-checkbox
            formControlName="isTrainerNotifiedOfNewTests"
            matTooltip="If checked, trainer receives an email each time a skill test is submitted for this quiz"
            >Skill-tests notifications by email</mat-checkbox
          >
        </div>
      </div>
      <mat-accordion
        cdkDropList
        (cdkDropListDropped)="reorderQuestions($event)"
      >
        <app-question-expansion-pannel
          *ngFor="let question of quiz?.questions"
          [question]="question"
          [expanded]="focusedQuestion === question"
          [isInEditMode$]="isInEditMode$"
          [isDragable]="canEditQuiz && !isInEditMode$"
          [skillTest]="skillTest"
          [isChoicesShuffled]="!!quiz?.isChoicesShuffled"
          [isPerQuestionResult]="!!quiz?.isPerQuestionResult"
          (onAnswerValidated)="focusNextUnasweredQuestion()"
          (onQuestionDeleted)="questionDeleted(question)"
          (opened)="setFocused(question)"
        >
        </app-question-expansion-pannel>
        <div style="width: 100%; display:inline-flex;">
          <span class="spacer"></span>
          <button
            *ngIf="canEditQuiz && (isInEditMode$ | async)"
            mat-fab
            color="primary"
            aria-label="Add a question"
            matTooltip="Add a question"
            class="item-button"
            (click)="openQuestionCreationDialog()"
          >
            <mat-icon>add</mat-icon>
          </button>
        </div>
      </mat-accordion>
      <div style="width: 100%; display: flex;">
        <span class="spacer"></span>
        <button
          *ngIf="
            isAuthenticated &&
            !(isInEditMode$ | async) &&
            (quiz?.isReplayEnabled || !isAnswerSubmitted)
          "
          [disabled]="!isTestComplete"
          (click)="submitAnswer()"
          mat-fab
          color="primary"
          aria-label="Submit answer"
          matTooltip="Submit answer"
          class="item-button"
        >
          <mat-icon>send</mat-icon>
        </button>
      </div>
    </div>`,
  styles: [],
})
export class QuizDetailsPage implements OnInit, OnDestroy {
  isLoading: boolean = false;
  quiz?: QuizDto;
  focusedQuestion?: QuestionDto;
  titleForm = new FormGroup({
    title: new FormControl('', [Validators.required]),
  });
  quizOptionsForm = new FormGroup({
    isChoicesShuffled: new FormControl<boolean>(true, []),
    isNotPerQuestionResult: new FormControl<boolean>(false, []),
    isReplayDisabled: new FormControl<boolean>(false, []),
    isTrainerNotifiedOfNewTests: new FormControl<boolean>(false, []),
  });
  isInEditMode$ = new BehaviorSubject<boolean>(false);
  skillTest!: SkillTestDto;
  isAnswerSubmitted: boolean = false;

  private titleFormSubscription?: Subscription;
  private optionsFormSubscription?: Subscription;
  private currentUserSubscription?: Subscription;

  constructor(
    private quizApi: QuizzesApi,
    private skillTestApi: SkillTestApi,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private user: UserService
  ) {}

  ngOnInit(): void {
    this.loadQuiz();

    this.activatedRoute.queryParamMap.subscribe((params) => {
      this.isInEditMode$.next(params.get('edit') === 'true');
    });
  }

  ngOnDestroy(): void {
    this.titleFormSubscription?.unsubscribe();
    this.optionsFormSubscription?.unsubscribe();
    this.currentUserSubscription?.unsubscribe();
  }

  get canEditQuiz(): boolean {
    return (
      this.quiz?.authorName === this.user.current.name && !this.quiz.isReplaced && (this.user.current.isModerator || !this.quiz?.isPublished)
    );
  }

  get canPublish(): boolean {
    return this.user.current.isModerator;
  }

  get isAuthenticated(): boolean {
    return this.user.current.isAuthenticated;
  }

  get isTrainer(): boolean {
    return this.user.current.isTrainer;
  }

  get isModerator(): boolean {
    return this.user.current.isModerator;
  }

  get isTestComplete(): boolean {
    return this.skillTest.questions.length === this.quiz?.questions?.length;
  }

  openQuestionCreationDialog() {
    const dialogRef = this.dialog.open(QuestionCreationDialog, {
      data: { quizId: this.quiz?.id },
    });
    dialogRef.afterClosed().subscribe((questionId) => {
      this.loadQuiz(questionId);
    });
  }

  setFocused(question?: QuestionDto) {
    this.focusedQuestion = question;
  }

  focusNextUnasweredQuestion() {
    const nextUnanswered = this.quiz?.questions?.find((q) => {
      const indexInAnswer = this.skillTest.questions.findIndex((answer) => {
        return answer.questionId === q.questionId;
      });
      return indexInAnswer < 0;
    });
    this.setFocused(nextUnanswered);
  }

  reorderQuestions(dragDrop: CdkDragDrop<QuestionDto[]>) {
    if (!this.quiz) {
      return;
    }
    moveItemInArray(
      this.quiz.questions,
      dragDrop.previousIndex,
      dragDrop.currentIndex
    );
    this.quizApi
      .updateQuestionsOrder(
        this.quiz.id,
        this.quiz.questions.map((q) => q.questionId)
      )
      .subscribe({
        error: (error) => this.dialog.open(ErrorDialog, { data: { error } }),
      });
  }

  questionDeleted(question: QuestionDto) {
    if (!!this.quiz) {
      this.quiz.questions = this.quiz.questions.filter(
        (q) => q.questionId !== question.questionId
      );
      this.skillTest.questions = this.skillTest.questions.filter(
        (q) => q.questionId !== question.questionId
      );
    }
  }

  createCopy() {
    if (!this.quiz?.id) {
      return;
    }
    this.quizApi.createDraft(this.quiz.id, 'response').subscribe({
      next: (response) => {
        const draftId = response.headers.get('Location');
        if (!!draftId) {
          this.isInEditMode$.next(true);
          this.router.navigate(['/', 'quizzes', draftId], {
            queryParams: { edit: true },
          });
        } else {
          this.dialog.open(ErrorDialog, {
            data: 'Failed to retrieve new draft ID.',
          });
        }
      },
      error: (error) => this.dialog.open(ErrorDialog, { data: error }),
    });
  }

  edit() {
    if (!!this.quiz?.isReplaced) {
      return;
    }
    if (!this.user.current.isModerator && !!this.quiz?.isPublished) {
      return;
    }
    this.isInEditMode$.next(true);
    this.loadQuiz();
  }

  editOff() {
    this.isInEditMode$.next(false);
    this.loadQuiz();
  }

  submitDraft() {
    if (!!this.quiz?.id) {
      this.isLoading = true;
      this.quizApi.submitDraft(this.quiz.id).subscribe({
        next: () => {
          this.isLoading = false;
          if (this.user.current.isModerator) {
            this.isInEditMode$.next(false);
          }
          this.loadQuiz();
        },
        error: (error) => {
          this.isLoading = false;
          this.dialog.open(ErrorDialog, { data: { error } });
        },
      });
    }
  }

  publishDraft() {
    if (!!this.quiz?.id) {
      this.isLoading = true;
      this.quizApi.publishDraft(this.quiz.id).subscribe({
        next: () => {
          this.isLoading = false;
          this.loadQuiz();
        },
        error: (error) => {
          this.isLoading = false;
          this.dialog.open(ErrorDialog, { data: { error } });
        },
      });
    }
  }

  rejectDraft() {
    const dialogRef = this.dialog.open(QuizRejectionDialog);
    dialogRef.afterClosed().subscribe((message) => {
      if (!!message) {
        if (!!this.quiz?.id) {
          this.isLoading = true;
          this.quizApi.rejectDraft(this.quiz.id, { message: '' }).subscribe({
            next: () => {
              this.isLoading = false;
              this.loadQuiz();
            },
            error: (error) => {
              this.isLoading = false;
              this.dialog.open(ErrorDialog, { data: { error } });
            },
          });
        }
      }
    });
  }

  private loadQuiz(selectedQuestionId?: number) {
    this.titleFormSubscription?.unsubscribe();
    this.optionsFormSubscription?.unsubscribe();
    this.activatedRoute.params.subscribe((parameter) => {
      this.skillTest = {
        quizId: parameter['quizId'],
        questions: [],
      };

      this.isLoading = true;
      this.quizApi.getQuiz(parameter['quizId']).subscribe({
        next: (dto) => {
          this.isLoading = false;
          this.quiz = dto;
          this.titleForm.controls.title.patchValue(dto.title);
          this.quizOptionsForm.controls.isChoicesShuffled.patchValue(
            dto.isChoicesShuffled
          );
          this.quizOptionsForm.controls.isNotPerQuestionResult.patchValue(
            !dto.isPerQuestionResult
          );
          this.quizOptionsForm.controls.isReplayDisabled.patchValue(
            !dto.isReplayEnabled
          );
          this.quizOptionsForm.controls.isTrainerNotifiedOfNewTests.patchValue(
            dto.isTrainerNotifiedOfNewTests
          );
          if (!!selectedQuestionId) {
            this.setFocused(
              this.quiz?.questions.find(
                (q) => q.questionId === selectedQuestionId
              ) || this.quiz?.questions?.at(0)
            );
          }
          if (!this.focusedQuestion) {
            this.setFocused(this.quiz?.questions?.at(0));
          }
          this.watchForms();
        },
        error: () => {
          this.isLoading = false;
          this.router.navigate(['/']);
        },
      });

      this.currentUserSubscription?.unsubscribe();
      this.currentUserSubscription = this.user.valueChanges.subscribe(
        (currentUser) => {
          if (currentUser.isAuthenticated) {
            this.isLoading = true;
            this.skillTestApi
              .getSkillTest(parameter['quizId'], currentUser.name)
              .subscribe({
                next: (dto) => {
                  this.isLoading = false;
                  this.isAnswerSubmitted = !!dto?.test?.quizId;
                },
                error: () => {
                  this.isLoading = false;
                  this.isAnswerSubmitted = false;
                },
              });
          }
        }
      );
    });
  }

  private watchForms() {
    this.titleFormSubscription = this.titleForm.controls[
      'title'
    ].valueChanges.subscribe((title) => {
      this.isLoading = true;
      this.quizApi
        .updateQuiz(this.quiz?.id || -1, {
          title: title || this.quiz?.title || '',
          isChoicesShuffled: !!this.quiz?.isChoicesShuffled,
          isPerQuestionResult: !!this.quiz?.isPerQuestionResult,
          isReplayEnabled: !!this.quiz?.isReplayEnabled,
          isTrainerNotifiedOfNewTests: !!this.quiz?.isTrainerNotifiedOfNewTests,
        })
        .subscribe({
          next: () => {
            this.isLoading = false;
            if (!!this.quiz) {
              this.quiz.title = title || '';
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.loadQuiz();
            this.dialog.open(ErrorDialog, { data: { error } });
          },
        });
    });

    this.optionsFormSubscription = this.quizOptionsForm.valueChanges.subscribe(
      (options) => {
        this.isLoading = true;
        this.quizApi
          .updateQuiz(this.quiz?.id || -1, {
            title: this.quiz?.title || '',
            isChoicesShuffled: options.isChoicesShuffled === true,
            isPerQuestionResult: !options.isNotPerQuestionResult,
            isReplayEnabled: !options.isReplayDisabled,
            isTrainerNotifiedOfNewTests:
              options.isTrainerNotifiedOfNewTests || false,
          })
          .subscribe({
            next: () => {
              this.isLoading = false;
              if (!!this.quiz) {
                this.quiz.isChoicesShuffled =
                  options.isChoicesShuffled === true;
                this.quiz.isPerQuestionResult = !options.isNotPerQuestionResult;
                this.quiz.isReplayEnabled = !options.isReplayDisabled;
              }
            },
            error: (error) => {
              this.isLoading = false;
              this.loadQuiz();
              this.dialog.open(ErrorDialog, { data: { error } });
            },
          });
      }
    );
  }

  submitAnswer() {
    this.isLoading = true;
    this.skillTestApi.submitSkillTest(this.skillTest, 'response').subscribe({
      next: (response) => {
        this.isLoading = false;
        this.dialog.open(SkillTestResultDialog, { data: {
          dto: response.body,
          testUri: response.headers.get('Location'),
          isAuthorNotified: !!this.quiz?.isTrainerNotifiedOfNewTests,
       }
      });
        this.isAnswerSubmitted = true;
      },
      error: (error) => {
        this.isLoading = false;
        this.dialog.open(ErrorDialog, { data: { error } });
      },
    });
  }
}
