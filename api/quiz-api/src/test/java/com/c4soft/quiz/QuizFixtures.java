package com.c4soft.quiz;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.Quiz;

public class QuizFixtures {

  public static Choice testChoice(Long quizId, Long questionId, Long choiceId) {
    return new Choice("Quiz %d, question %d, choice %d".formatted(quizId, questionId, choiceId),
        (choiceId % 2) != 0);
  }

  public static Question testQuestion(Long quizId, Long questionId) {
    final var question = new Question("Quiz %d, question %d".formatted(quizId, questionId),
        "Yest another interesting question", questionId.intValue(), "This is why",
        testChoice(quizId, questionId, questionId * 2),
        testChoice(quizId, questionId, questionId * 2 + 1));
        question.setPriority(questionId.intValue() / 2);
        return question;
  }

  public static Quiz testQuiz(Long quizId, String authorName, boolean isPublished) {
    final var quiz = new Quiz("Quiz %d".formatted(quizId), authorName,
        testQuestion(quizId, quizId * 2), testQuestion(quizId, quizId * 2 + 1));
    quiz.setIsPublished(isPublished);
    return quiz;
  }


}
