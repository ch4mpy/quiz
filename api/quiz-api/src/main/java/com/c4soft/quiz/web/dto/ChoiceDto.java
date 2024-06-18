/* (C)2024 */
package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ChoiceDto(@NotNull Long quizId, @NotNull Long questionId, @NotNull Long choiceId,
    @NotEmpty String label, boolean isGood) {
}
