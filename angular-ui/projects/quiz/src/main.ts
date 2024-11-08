import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { importProvidersFrom, provideExperimentalZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app/app-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ApiModule as QuizApiModule, Configuration as QuizApiConfiguration, ConfigurationParameters as QuizApiConfigurationParameters } from '@c4-soft/quiz-api';
import { ApiModule as BffApiModule, Configuration as BffApiConfiguration, ConfigurationParameters as BffApiConfigurationParameters } from '@c4-soft/bff-api';
import { DatePipe } from '@angular/common';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MAT_MOMENT_DATE_FORMATS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { QuillModule } from 'ngx-quill';

export function bffApiConfigFactory(): BffApiConfiguration {
  const params: BffApiConfigurationParameters = {
      basePath: '/bff',
  };
  return new BffApiConfiguration(params);
}

export function quizApiConfigFactory(): QuizApiConfiguration {
  const params: QuizApiConfigurationParameters = {
      basePath: '/bff/v1',
  };
  return new QuizApiConfiguration(params);
}

bootstrapApplication(AppComponent, {
  providers: [
      provideExperimentalZonelessChangeDetection(),
      provideRouter(routes),
      provideHttpClient(withInterceptorsFromDi()),
      importProvidersFrom(
          BrowserAnimationsModule,
          BffApiModule.forRoot(bffApiConfigFactory),
          QuizApiModule.forRoot(quizApiConfigFactory),
          QuillModule.forRoot(),
      ),
      DatePipe,
      {
          provide: DateAdapter,
          useClass: MomentDateAdapter,
          deps: [MAT_DATE_LOCALE],
      },
      { provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS },
  ],
});
