package com.c4soft.quiz.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotADraftException extends RuntimeException {
	private static final long serialVersionUID = -8566841487060787834L;

	public NotADraftException(Long quizId) {
		super("Quiz %d is not a draft".formatted(quizId));
	}

	
}
