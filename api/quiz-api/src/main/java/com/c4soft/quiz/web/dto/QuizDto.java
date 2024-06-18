/* (C)2024 */
package com.c4soft.quiz.web.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @param id unique identifier for the quiz
 * @param title the quiz title
 * @param questions an array with all of the quiz questions
 * @param authorName name of the trainer who authored the quiz
 * @param isPublished is the quiz available for trainees
 * @param isSubmitted is the quiz submitted to moderation
 * @param isReplaced was a new version of this quiz published (and replaces this one)
 * @param isChoicesShuffled should the choices display order be shuffled from a question display to
 *        another
 * @param isReplayEnabled can a trainee submit a new answer before his former one was deleted by the
 *        trainer
 * @param isPerQuestionResult should the right answer as well as comment be displayed as soon as
 *        choices are validated for a question or only when the skill test was accepted by the
 *        server
 * @param isTrainerNotifiedOfNewTests if true, trainers receive an email each time a new skill-test
 *        is submitted (for quizzes they authored only)
 * @param ModeratorComment an explanation why this quiz version was rejected by a moderator
 * @param draftId unique identifier for a modified version of this quiz
 * @param replacesId identifier of the former version of this quiz
 */
public record QuizDto(@NotNull Long id, @NotEmpty String title,
    @NotNull List<QuestionDto> questions, @NotEmpty String authorName, @NotNull Boolean isPublished,
    @NotNull Boolean isSubmitted, @NotNull Boolean isReplaced, @NotNull Boolean isChoicesShuffled,
    @NotNull Boolean isReplayEnabled, @NotNull Boolean isPerQuestionResult,
    @NotNull Boolean isTrainerNotifiedOfNewTests, String moderatorComment, Long draftId,
    Long replacesId) {
}
