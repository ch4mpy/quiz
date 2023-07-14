package com.c4soft.quiz.web;

import jakarta.validation.constraints.NotEmpty;

public record ChoiceUpdateDto(@NotEmpty String label, boolean isGood) {
}