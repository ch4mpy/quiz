package com.c4soft.quiz.web;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record QuestionDto(@NotNull Long quizId, @NotNull Long questionId, String label, List<ChoiceDto> choices,
	@NotNull String comment) {
}