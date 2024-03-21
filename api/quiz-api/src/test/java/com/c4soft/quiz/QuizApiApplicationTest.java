package com.c4soft.quiz;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AddonsWebmvcTestConf;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.ChoiceRepository;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.QuestionRepository;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizRepository;
import com.c4soft.quiz.feign.KeycloakAdminApiClient;
import com.c4soft.quiz.feign.KeycloakAdminApiClient.UserRepresentation;
import com.c4soft.quiz.web.dto.ChoiceUpdateDto;
import com.c4soft.quiz.web.dto.QuestionUpdateDto;
import com.c4soft.quiz.web.dto.QuizUpdateDto;
import com.c4soft.quiz.web.dto.SkillTestDto;
import com.c4soft.quiz.web.dto.SkillTestQuestionDto;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@SpringBootTest
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@Import(AddonsWebmvcTestConf.class)
class QuizApiApplicationTest {
	@Autowired
	QuizRepository quizRepo;

	@Autowired
	QuestionRepository questionRepo;

	@Autowired
	ChoiceRepository choiceRepo;

	@Autowired
	MockMvcSupport api;

	@MockBean
	KeycloakAdminApiClient keycloakAdminApi;

	@BeforeEach
	void setUp() {
		quizRepo.save(Fixtures.openIdTraingQuiz());
	}

	@Test
	@WithJwt("ch4mp.json")
	void givenUserIsCh4mp_whenGoingThroughQuizNominalCrudOperations_thenOk() throws UnsupportedEncodingException, Exception {
		// Database is initialized with 1 quiz
		var actual = parse(api.get("/quizzes").andExpect(status().isOk()).andReturn(), JSONArray.class);
		assertEquals(1L, actual.size());
		assertEquals("OAuth2 and OpenID in web echosystem with Spring", ((JSONObject) actual.get(0)).get("title"));

		// Create a draft from existing quiz
		final var draftId = api
				.post(null, "/quizzes/{quiz-id}/draft", ((JSONObject) actual.get(0)).get("id"))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		actual = parse(api.get("/quizzes").andExpect(status().isOk()).andReturn(), JSONArray.class);
		assertEquals(2L, actual.size());

		// Update draft title
		api.put(new QuizUpdateDto("Updated title", true, true, true, false), "/quizzes/{quiz-id}", draftId).andExpect(status().isAccepted());

		// Submit draft to moderation (with auto-publication as ch4mp is both trainer and moderator
		api.put(null, "/quizzes/{quiz-id}/submit", draftId).andExpect(status().isAccepted());
		actual = parse(api.get("/quizzes").andExpect(status().isOk()).andReturn(), JSONArray.class);
		assertEquals(2L, actual.size());
		assertEquals(1L, actual.stream().filter(q -> (boolean) ((JSONObject) q).get("isPublished")).toList().size());

		// Create a new quiz
		final var quiz1Id = api
				.post(new QuizUpdateDto("Second Quiz", true, true, true, false), "/quizzes")
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		api.get("/quizzes?titleLike=second").andExpect(status().isOk()).andExpect(jsonPath("$.*.title", hasItems("Second Quiz")));

		// Add 2 questions to the new quiz
		final var question10Id = api
				.post(new QuestionUpdateDto("machin", "", "truc"), "/quizzes/{quiz-id}/questions", quiz1Id)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		final var question11Id = api
				.post(new QuestionUpdateDto("bidule", "", "chode"), "/quizzes/{quiz-id}/questions", quiz1Id)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isOk()).andExpect(jsonPath("$.questions.*.label", hasItems("machin", "bidule")));

		// Reverse questions order
		api.put(List.of(question11Id, question10Id), "/quizzes/{quiz-id}/questions", quiz1Id).andExpect(status().isAccepted());

		// Update the 1st question title and comment
		api
				.put(
						new QuestionUpdateDto("What is the answer to machin?", "", "The answer is truc."),
						"/quizzes/{quiz-id}/questions/{question-id}",
						quiz1Id,
						question10Id)
				.andExpect(status().isAccepted());
		api
				.get("/quizzes/{quiz-id}", quiz1Id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.questions[0].label", is("bidule")))
				.andExpect(jsonPath("$.questions[1].label", is("What is the answer to machin?")));

		// Add 2 choices
		final var choice100Id = api
				.post(new ChoiceUpdateDto("truc", true), "/quizzes/{quiz-id}/questions/{question-id}/choices", quiz1Id, question10Id)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		final var choice101Id = api
				.post(new ChoiceUpdateDto("chouette", false), "/quizzes/{quiz-id}/questions/{question-id}/choices", quiz1Id, question10Id)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isOk()).andExpect(jsonPath("$.questions[1].choices.*.label", hasItems("truc", "chouette")));
		api
				.put(new ChoiceUpdateDto("chose", true), "/quizzes/{quiz-id}/questions/{question-id}/choices/{choice-id}", quiz1Id, question10Id, choice101Id)
				.andExpect(status().isAccepted());
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isOk()).andExpect(jsonPath("$.questions[1].choices.*.label", hasItems("truc", "chose")));

		// Submit draft to moderation (with auto-publication as ch4mp is both trainer and moderator
		api.put(null, "/quizzes/{quiz-id}/submit", quiz1Id).andExpect(status().isAccepted());
		actual = parse(api.get("/quizzes").andExpect(status().isOk()).andReturn(), JSONArray.class);
		assertEquals(3L, actual.size());
		assertEquals(2L, actual.stream().filter(q -> (boolean) ((JSONObject) q).get("isPublished")).toList().size());

		// Delete a question
		api.delete("/quizzes/{quiz-id}/questions/{question-id}/choices/{choice-id}", quiz1Id, question10Id, choice100Id).andExpect(status().isAccepted());
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isOk()).andExpect(jsonPath("$.questions[1].choices.*.label", hasSize(1)));

		// Delete the other question
		api.delete("/quizzes/{quiz-id}/questions/{question-id}", quiz1Id, question10Id).andExpect(status().isAccepted());
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isOk()).andExpect(jsonPath("$.questions.*.label", hasSize(1)));

		// Delete the new quiz
		api.delete("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isAccepted());
		api.get("/quizzes").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
		api.get("/quizzes/{quiz-id}", draftId).andExpect(status().isOk()).andExpect(jsonPath("$.title", is("Updated title")));
		api.get("/quizzes/{quiz-id}", quiz1Id).andExpect(status().isNotFound());
	}

	@Test
	@WithJwt("tonton-pirate.json")
	void givenUserIsATrainee_whenGoingThroughSkillTestNominalOperations_thenOk() throws UnsupportedEncodingException, Exception {
		var quiz = (JSONObject) parse(api.get("/quizzes").andExpect(status().isOk()).andReturn(), JSONArray.class).get(0);
		final var quizId = Long.valueOf(quiz.get("id").toString());

		final var worstPossibleAnswer = new SkillTestDto(quizId, new ArrayList<>());
		final var perfectAnswer = new SkillTestDto(quizId, new ArrayList<>());
		for (var q : (JSONArray) quiz.get("questions")) {
			final var question = (JSONObject) q;
			final var questionId = Long.valueOf(question.get("questionId").toString());
			final var questionBestAnswer = new SkillTestQuestionDto(questionId, new ArrayList<>());
			final var questionWorstAnswer = new SkillTestQuestionDto(questionId, new ArrayList<>());
			for (var c : (JSONArray) question.get("choices")) {
				final var choice = (JSONObject) c;
				final var choiceId = Long.valueOf(choice.get("choiceId").toString());
				if ((Boolean) choice.get("isGood")) {
					questionBestAnswer.choices().add(choiceId);
				} else {
					questionWorstAnswer.choices().add(choiceId);
				}
			}
			perfectAnswer.questions().add(questionBestAnswer);
			worstPossibleAnswer.questions().add(questionWorstAnswer);
		}

		api.put(worstPossibleAnswer, "/skill-tests").andExpect(status().isAccepted()).andExpect(jsonPath("$.score", is(-50.0)));
		when(keycloakAdminApi.getUser("tonton-pirate", true)).thenReturn(List.of(new UserRepresentation(Map.of("email", "tonton-pirate@c4-soft.com"))));
		api
				.perform(get("/skill-tests/{quizId}/{traineeName}", quizId.toString(), "tonton-pirate"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.score", is(-50.0)));

		api.put(perfectAnswer, "/skill-tests").andExpect(status().isAccepted()).andExpect(jsonPath("$.score", is(100.0)));
		api
				.perform(get("/skill-tests/{quizId}/{traineeName}", quizId.toString(), "tonton-pirate"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.score", is(100.0)));

	}

	private <T> T parse(MvcResult result, Class<T> clazz) throws UnsupportedEncodingException, ParseException {
		return new JSONParser(JSONParser.MODE_PERMISSIVE).parse(result.getResponse().getContentAsString(), clazz);
	}

	public static class Fixtures {
		static final String trainer1 = "ch4mp";
		static final String moderator1 = "ch4mp";

		public static Quiz openIdTraingQuiz() {
			final var quiz = new Quiz(
					"OAuth2 and OpenID in web echosystem with Spring",
					trainer1,
					new Question("Question 1", "", 0, "Good is right", new Choice("good", true), new Choice("bad", false)),
					new Question("Question 2", "", 1, "Bad is wrong", new Choice("bad", false), new Choice("good", true)));
			quiz.setModeratedBy(moderator1);
			quiz.setIsPublished(true);
			return quiz;
		}
	}

}
