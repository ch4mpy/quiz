package com.c4soft.quiz.web;

import jakarta.validation.constraints.NotNull;

public record SkillTestResultDto(@NotNull SkillTestDto test, @NotNull Double score) {}