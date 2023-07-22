import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule, routes } from './app-routing.module';
import { AppComponent } from './app.component';

import {
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter,
} from '@angular/material-moment-adapter';
import {
  Configuration as BffApiConfiguration,
  ConfigurationParameters as BffApiConfigurationParameters,
  ApiModule as BffApiModule,
} from '@c4-soft/bff-api';

import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
} from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import {
  Configuration as QuizApiConfiguration,
  ConfigurationParameters as QuizApiConfigurationParameters,
  ApiModule as QuizApiModule,
} from '@c4-soft/quiz-api';
import { ChoiceItemComponent } from './choice-item.component';
import { ConfirmationDialog } from './confirmation.dialog';
import { ErrorDialog } from './error.dialog';
import { QuestionCreationDialog } from './question-creation.dialog';
import { QuestionExpansionPannelComponent } from './question-expansion-pannel.component';
import { QuizCreationDialog } from './quiz-creation.dialog';
import { QuizDetailsPage } from './quiz-details.page';
import { QuizRejectionDialog } from './quiz-rejection.dialog';
import { QuizSelectionPage } from './quiz-selection.page';
import { SkillTestDetailsPage } from './skill-test-details.page';
import { SkillTestSelectionPage } from './skill-test-selection.page';
import { ToolbarComponent } from './toolbar.component';
import { SkillTestResultDialog } from './skill-test-result.dialog';

export function bffApiConfigFactory(): BffApiConfiguration {
  const params: BffApiConfigurationParameters = {
    basePath: '',
  };
  return new BffApiConfiguration(params);
}

export function quizApiConfigFactory(): QuizApiConfiguration {
  const params: QuizApiConfigurationParameters = {
    basePath: '/bff/v1',
  };
  return new QuizApiConfiguration(params);
}

@NgModule({
  declarations: [
    AppComponent,
    ToolbarComponent,
    QuizSelectionPage,
    QuizCreationDialog,
    ErrorDialog,
    QuizDetailsPage,
    QuestionCreationDialog,
    ChoiceItemComponent,
    QuestionExpansionPannelComponent,
    ConfirmationDialog,
    QuizRejectionDialog,
    SkillTestSelectionPage,
    SkillTestDetailsPage,
    SkillTestResultDialog,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    BffApiModule.forRoot(bffApiConfigFactory),
    QuizApiModule.forRoot(quizApiConfigFactory),
    DragDropModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatMomentDateModule,
    MatProgressBarModule,
    MatSelectModule,
    MatToolbarModule,
    MatTooltipModule,
  ],
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
