import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SkillTestResultPreviewDto } from '@c4-soft/quiz-api';

@Component({
  selector: 'app-skill-test-result',
  template: `<mat-toolbar color="primary">
  <span>Answer saved</span>
</mat-toolbar>
<div style="margin: 3em;">
  <div>Score: {{ data.score }}</div>
  <div>Your trainer was notified</div>
</div>`,
  styles: [
  ]
})
export class SkillTestResultDialog {

  constructor(@Inject(MAT_DIALOG_DATA) public data: SkillTestResultPreviewDto) {}
}
