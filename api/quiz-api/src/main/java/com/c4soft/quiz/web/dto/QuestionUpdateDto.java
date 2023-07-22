package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @parameter label the new label for the question
 * @parameter comment a new explanation for the right answer
 */
public record QuestionUpdateDto(@NotEmpty String label, @NotNull String comment) {
}