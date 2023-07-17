package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotNull;

public record SkillTestResultDto(@NotNull SkillTestDto test, @NotNull Double score) {}