package com.c4soft.quiz.web;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.quiz.EnableSpringDataWebSupportTestConf;
import com.c4soft.quiz.SecurityConfig;
import com.c4soft.quiz.web.dto.QuizUpdateDto;

@WebMvcTest(controllers = QuizController.class)
@ActiveProfiles("h2")
@Import({ EnableSpringDataWebSupportTestConf.class, ExceptionHandlers.class, SecurityConfig.class })
@AutoConfigureAddonsWebmvcResourceServerSecurity
class QuizControllerTest {
	@Autowired
	MockMvcSupport api;

	@Test
	@WithJwt("ch4mp.json")
	void givenUserIsCh4mp_whenPayloadIsInvalid_then422WithValidationExceptionsInProblemDetails() throws Exception {
		api
				.post(new QuizUpdateDto("", null, null, null, null), "/quizzes")
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.invalidFields.title", is("NotEmpty")))
				.andExpect(jsonPath("$.invalidFields.isChoicesShuffled", is("NotNull")))
				.andExpect(jsonPath("$.invalidFields.isReplayEnabled", is("NotNull")))
				.andExpect(jsonPath("$.invalidFields.isPerQuestionResult", is("NotNull")))
				.andExpect(jsonPath("$.invalidFields.isTrainerNotifiedOfNewTests", is("NotNull")));
	}
}
