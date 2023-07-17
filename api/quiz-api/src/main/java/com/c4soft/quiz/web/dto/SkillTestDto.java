package com.c4soft.quiz.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SkillTestDto(@NotNull Long quizId, @NotEmpty List<SkillTestQuestionDto> questions) {
    public SkillTestQuestionDto getQuestion(Long questionId) {
	return questions.stream().filter(q -> q.questionId() == questionId).findAny().orElse(new SkillTestQuestionDto(questionId, List.of()));
    }
}