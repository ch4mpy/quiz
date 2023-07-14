package com.c4soft.quiz.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QuestionUpdateDto(@NotEmpty String label, @NotNull String comment) {
}