package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @param title                       the new title for the quiz
 * @param isChoicesShuffled           should choices display order be randomized from a display to another.
 * @param isReplayEnabled             can a trainee submit a new skill test before the former one was deleted by the trainer.
 * @param isPerQuestionResult         if true, the right answer as well as comment should be displayed as soon as choices for a question are validated.
 *                                    Otherwise, it should be displayed only when the test was accepted by the server.
 * @param isTrainerNotifiedOfNewTests if true, trainers receive an email each time a new skill-test is submitted (for quizzes they authored only)
 */
public record QuizUpdateDto(
		@NotEmpty String title,
		@NotNull Boolean isChoicesShuffled,
		@NotNull Boolean isReplayEnabled,
		@NotNull Boolean isPerQuestionResult,
		@NotNull Boolean isTrainerNotifiedOfNewTests) {
}