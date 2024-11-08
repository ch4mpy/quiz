import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PrivacyPolicyPage } from './privacy-policy.page';
import { QuizDetailsPage } from './quiz-details.page';
import { QuizSelectionPage } from './quiz-selection.page';
import { SkillTestDetailsPage } from './skill-test-details.page';
import { SkillTestSelectionPage } from './skill-test-selection.page';

export const routes: Routes = [
  { path: 'quizzes', component: QuizSelectionPage },
  { path: 'quizzes/:quizId', component: QuizDetailsPage },
  { path: 'privacy', component: PrivacyPolicyPage },
  { path: 'tests', component: SkillTestSelectionPage },
  { path: 'tests/:quizId/:traineeName', component: SkillTestDetailsPage },
  { path: '**', redirectTo: '/quizzes' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
