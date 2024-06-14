/* (C)2024 */
package com.c4soft.quiz.domain;

import jakarta.validation.constraints.NotEmpty;

public record QuizRejectionDto(
    @NotEmpty(message = "A non-empty message is mandatory") String message) {
}
