import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import {
  Configuration as BffApiConfiguration,
  ConfigurationParameters as BffApiConfigurationParameters,
  ApiModule as BffApiModule,
} from '@c4-soft/bff-api';

import { HttpClientModule } from '@angular/common/http';
import {
  Configuration as QuizApiConfiguration,
  ConfigurationParameters as QuizApiConfigurationParameters,
  ApiModule as QuizApiModule,
} from '@c4-soft/quiz-api';
import { ToolbarComponent } from './toolbar.component';

export function bffApiConfigFactory(): BffApiConfiguration {
  const params: BffApiConfigurationParameters = {
    basePath: '',
  };
  return new BffApiConfiguration(params);
}

export function quizApiConfigFactory(): QuizApiConfiguration {
  const params: QuizApiConfigurationParameters = {
    basePath: '/bff/quizzes/v1/',
  };
  return new QuizApiConfiguration(params);
}

@NgModule({
  declarations: [AppComponent, ToolbarComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    BffApiModule.forRoot(bffApiConfigFactory),
    QuizApiModule.forRoot(quizApiConfigFactory),
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatToolbarModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
