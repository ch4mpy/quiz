package com.c4soft.quiz.domain.exception;

public class InvalidQuizException extends RuntimeException {
  private static final long serialVersionUID = 8816930385638385805L;

  public InvalidQuizException(Long quizId) {
    super("Quiz %d doesn't accept answers anymore.".formatted(quizId));
  }
}
