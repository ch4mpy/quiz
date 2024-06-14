/* (C)2024 */
package com.c4soft.quiz.web.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record QuestionDto(@NotNull Long quizId, @NotNull Long questionId, @NotNull Long priority,
    String label, String formattedBody, List<ChoiceDto> choices, @NotNull String comment) {
}
