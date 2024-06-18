/* (C)2024 */
package com.c4soft.quiz.domain.exception;

public class DraftAlreadyExistsException extends RuntimeException {
  private static final long serialVersionUID = -8566841487060787834L;

  public DraftAlreadyExistsException(Long quizId) {
    super("A draft already exists for quiz %d".formatted(quizId));
  }
}
