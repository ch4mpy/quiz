package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QuestionUpdateDto(@NotEmpty String label, @NotNull String comment) {
}