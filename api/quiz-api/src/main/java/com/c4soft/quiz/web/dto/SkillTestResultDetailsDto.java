package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotNull;

public record SkillTestResultDetailsDto(
		@NotNull SkillTestDto test,
		@NotNull String traineeUsername,
		@NotNull String traineeFirstName,
		@NotNull String traineeLastName,
		@NotNull String traineeEmail,
		@NotNull Double score) {
}