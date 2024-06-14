/* (C)2024 */
package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SkillTestQuestionDto(@NotNull Long questionId, List<Long> choices) {}
