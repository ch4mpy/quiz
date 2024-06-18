/* (C)2024 */
package com.c4soft.quiz.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.quiz.EnableSpringDataWebSupportTestConf;
import com.c4soft.quiz.ExceptionHandlers;
import com.c4soft.quiz.QuizFixtures;
import com.c4soft.quiz.SecurityConfig;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.jpa.ChoiceRepository;
import com.c4soft.quiz.domain.jpa.QuestionRepository;
import com.c4soft.quiz.domain.jpa.QuizRepository;
import com.c4soft.quiz.domain.jpa.SkillTestRepository;
import com.c4soft.quiz.web.dto.QuizUpdateDto;
import com.c4soft.quiz.web.dto.mapping.ChoiceMapperImpl;
import com.c4soft.quiz.web.dto.mapping.QuestionMapperImpl;
import com.c4soft.quiz.web.dto.mapping.QuizMapperImpl;

@WebMvcTest(controllers = QuizController.class, properties = {"hostname=localhost"})
@ActiveProfiles("h2")
@Import({EnableSpringDataWebSupportTestConf.class, ExceptionHandlers.class, SecurityConfig.class,
    QuizMapperImpl.class, QuestionMapperImpl.class, ChoiceMapperImpl.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
class QuizControllerTest {
  @Autowired
  MockMvcSupport api;

  @Autowired
  QuizRepository quizRepo;

  @Autowired
  QuestionRepository questionRepo;

  @Autowired
  ChoiceRepository choiceRepo;

  @Autowired
  SkillTestRepository skillTestRepo;

  Quiz quiz1;
  Quiz quiz2;
  Quiz quiz3;

  @BeforeEach
  @Transactional(readOnly = false)
  void setup() {
    quiz1 = QuizFixtures.testQuiz(1L, "ch4mp", true);
    quiz2 = QuizFixtures.testQuiz(2L, "vickette", true);
    quiz3 = QuizFixtures.testQuiz(3L, "ch4mp", true);
    skillTestRepo.deleteAll();
    quizRepo.deleteAll();
    quizRepo.saveAllAndFlush(List.of(quiz1, quiz2, quiz3));
  }

  @Test
  @WithJwt("ch4mp.json")
  void givenUserIsCh4mp_whenPayloadIsInvalid_then422WithValidationExceptionsInProblemDetails()
      throws Exception {
    api.post(new QuizUpdateDto("", null, null, null, null), "/quizzes")
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.invalidFields.title", is("NotEmpty")))
        .andExpect(jsonPath("$.invalidFields.isChoicesShuffled", is("NotNull")))
        .andExpect(jsonPath("$.invalidFields.isReplayEnabled", is("NotNull")))
        .andExpect(jsonPath("$.invalidFields.isPerQuestionResult", is("NotNull")))
        .andExpect(jsonPath("$.invalidFields.isTrainerNotifiedOfNewTests", is("NotNull")));
    assertEquals(3, quizRepo.count());
    assertEquals(6, questionRepo.count());
    assertEquals(12, choiceRepo.count());
  }

  @Test
  @WithJwt("unprivieldged-trainer.json")
  void givenUserIsCh4mp_whenPayloadIsValid_thenCreated() throws Exception {
    final var quizzesCount = quizRepo.count();

    final var location = api
        .post(new QuizUpdateDto("Test quiz", true, false, false, false), "/quizzes")
        .andExpect(status().isCreated()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

    assertFalse(location.isEmpty());
    assertEquals(quizzesCount + 1, quizRepo.count());
    assertTrue(quizRepo.findById(Long.parseLong(location)).isPresent());

    assertEquals(quizzesCount * 2, questionRepo.count());
    assertEquals(quizzesCount * 4, choiceRepo.count());
  }

  @Test
  @WithJwt("trainee.json")
  void givenUserIsTrainee_whenPayloadIsValid_thenForbidden() throws Exception {
    final var quizzesCount = quizRepo.count();

    api.post(new QuizUpdateDto("Test quiz", true, false, false, false), "/quizzes")
        .andExpect(status().isForbidden());

    assertEquals(quizzesCount, quizRepo.count());
    assertEquals(quizzesCount * 2, questionRepo.count());
    assertEquals(quizzesCount * 4, choiceRepo.count());
  }

  @Test
  @WithAnonymousUser
  void givenRequestIsAnonymous_whenGetQuizList_thenOk() throws Exception {
    api.perform(get("/quizzes")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));

    api.perform(get("/quizzes").param("authorLike", "4").param("titleLike", "3"))
        .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is(quiz3.getTitle())));

    assertEquals(3, quizRepo.count());
    assertEquals(6, questionRepo.count());
    assertEquals(12, choiceRepo.count());
  }
}
