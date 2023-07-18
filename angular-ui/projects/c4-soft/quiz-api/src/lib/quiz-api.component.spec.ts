import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizApiComponent } from './quiz-api.component';

describe('QuizApiComponent', () => {
  let component: QuizApiComponent;
  let fixture: ComponentFixture<QuizApiComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [QuizApiComponent]
    });
    fixture = TestBed.createComponent(QuizApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
