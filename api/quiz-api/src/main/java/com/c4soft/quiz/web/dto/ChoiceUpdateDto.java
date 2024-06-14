/* (C)2024 */
package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ChoiceUpdateDto(@NotEmpty @Size(max = 255) String label, boolean isGood) {
}
