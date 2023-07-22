package com.c4soft.quiz.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DraftAlreadyExistsException extends RuntimeException {
	private static final long serialVersionUID = -8566841487060787834L;

	public DraftAlreadyExistsException(Long quizId) {
		super("A draft already exists for quiz %d".formatted(quizId));
	}

	
}
