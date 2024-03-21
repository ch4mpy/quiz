package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @parameter label the new label for the question
 * @parameter comment a new explanation for the right answer
 */
public record QuestionUpdateDto(@NotEmpty @Size(max = 255) String label, @NotNull @Size(max = 2047) String formattedBody, @NotNull @Size(max = 255) String comment) {
}