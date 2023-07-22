import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuizSelectionPage } from './quiz-selection.page';
import { QuizDetailsPage } from './quiz-details.page';
import { SkillTestSelectionPage } from './skill-test-selection.page';
import { SkillTestDetailsPage } from './skill-test-details.page';

export const routes: Routes = [
  { path: 'quizzes', component: QuizSelectionPage },
  { path: 'quizzes/:quizId', component: QuizDetailsPage },
  { path: 'tests', component: SkillTestSelectionPage },
  { path: 'tests/:quizId/:traineeName', component: SkillTestDetailsPage },
  { path: '**',   redirectTo: '/quizzes' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
