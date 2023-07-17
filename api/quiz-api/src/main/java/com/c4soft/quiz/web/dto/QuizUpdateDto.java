package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;

public record QuizUpdateDto(@NotEmpty String title) {
}