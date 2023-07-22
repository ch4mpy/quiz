package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotNull;

public record SkillTestResultPreviewDto(@NotNull String traineeName, @NotNull Double score) {}