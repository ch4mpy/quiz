import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SkillTestResultPreviewDto } from '@c4-soft/quiz-api';

export interface SkillTestResultDialogData {
  dto: SkillTestResultPreviewDto;
  testUri: string;
  isAuthorNotified: boolean;
}
@Component({
  standalone: true,
  selector: 'app-skill-test-result',
  imports: [
    CommonModule,
    MatToolbarModule,
  ],
  template: `<mat-toolbar color="primary">
  <span>Answer saved</span>
</mat-toolbar>
<div style="margin: 3em;">
  <div>Score: {{ data.dto.score }}</div>
  <div *ngIf="data.isAuthorNotified">Your trainer was notified</div>
  <div>Details: <a [href]="data.testUri">{{ data.testUri }}</a></div>
</div>`,
  styles: [
  ]
})
export class SkillTestResultDialog {

  constructor(@Inject(MAT_DIALOG_DATA) public data: SkillTestResultDialogData) {}
}
