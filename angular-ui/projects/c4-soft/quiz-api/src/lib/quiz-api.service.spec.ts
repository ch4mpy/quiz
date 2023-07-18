import { TestBed } from '@angular/core/testing';

import { QuizApiService } from './quiz-api.service';

describe('QuizApiService', () => {
  let service: QuizApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QuizApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
