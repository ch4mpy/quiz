package com.c4soft.quiz.domain.exception;

public class QuizAlreadyHasAnAnswerException extends RuntimeException {

  private static final long serialVersionUID = 6171083302507116601L;

  public QuizAlreadyHasAnAnswerException(Long quizId, String traineeName) {
    super(
        "Quiz %d already has an answer for %s and doesn't accept replay. Ask the trainer to delete the answer before submitting a new one."
            .formatted(quizId, traineeName));
  }
}
