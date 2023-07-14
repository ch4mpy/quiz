package com.c4soft.quiz.web;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record SkillTestQuestionDto(@NotNull Long questionId, List<Long> choices) {
}