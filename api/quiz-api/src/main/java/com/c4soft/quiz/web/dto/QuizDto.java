package com.c4soft.quiz.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QuizDto(@NotNull Long id, @NotEmpty String title, @NotNull List<QuestionDto> questions) {
}