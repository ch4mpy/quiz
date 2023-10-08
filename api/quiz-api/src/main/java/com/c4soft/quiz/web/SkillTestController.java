package com.c4soft.quiz.web;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizRepository;
import com.c4soft.quiz.domain.SkillTest;
import com.c4soft.quiz.domain.SkillTest.SkillTestPk;
import com.c4soft.quiz.domain.SkillTestRepository;
import com.c4soft.quiz.feign.KeycloakAdminApiClient;
import com.c4soft.quiz.web.dto.SkillTestDto;
import com.c4soft.quiz.web.dto.SkillTestQuestionDto;
import com.c4soft.quiz.web.dto.SkillTestResultDetailsDto;
import com.c4soft.quiz.web.dto.SkillTestResultPreviewDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/skill-tests")
@RequiredArgsConstructor
@Validated
@Tag(name = "SkillTest")
public class SkillTestController {
	private final SkillTestRepository testRepo;
	private final QuizRepository quizRepo;
	private final KeycloakAdminApiClient keycloakAdminApi;
	private final JavaMailSender mailSender;
	@Value("${ui-external-uri}")
	URI uiUri;

	@GetMapping(path = "/{quizId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	@Operation(description = "Returns the answers to a quiz, by default for all trainees over the last 2 weeks")
	public List<SkillTestResultPreviewDto> getSkillTestList(
			@PathVariable(value = "quizId", required = true) Long quizId,
			@RequestParam(value = "since", required = false) Optional<Long> since,
			@RequestParam(value = "until", required = false) Optional<Long> until) {
		final var resolvedSince = since.map(sec -> 1000 * sec).orElse(Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli());
		final var tests = testRepo.findAll(SkillTestRepository.spec(quizId, resolvedSince, until.map(sec -> 1000 * sec))).stream();
		return tests.map(this::toPreviewDto).toList();
	}

	@GetMapping(path = "/{quizId}/{traineeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	@Operation(description = "Returns the answers to a quiz, by default for all trainees over the last 2 weeks")
	public SkillTestResultDetailsDto getSkillTest(
			@PathVariable(value = "quizId", required = true) Long quizId,
			@PathVariable(value = "traineeName", required = true) String traineeName) {
		final var skillTest = testRepo.findById(new SkillTestPk(quizId, traineeName));
		if (skillTest.isEmpty()) {
			throw new EntityNotFoundException("No skill-test for quiz %d and trainee %s".formatted(quizId, traineeName));
		}
		return toDetailsDto(skillTest.get());
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = false)
	public ResponseEntity<SkillTestResultPreviewDto> submitSkillTest(@RequestBody @Valid SkillTestDto dto, Authentication auth) {
		final var quiz = quizRepo.findById(dto.quizId()).orElseThrow(() -> new InvalidQuizException(dto.quizId()));
		if (!quiz.getIsPublished()) {
			throw new InvalidQuizException(dto.quizId());
		}
		if (!quiz.getIsReplayEnabled() && testRepo.findByIdQuizIdAndIdTraineeName(dto.quizId(), auth.getName()).isPresent()) {
			throw new QuizAlreadyHasAnAnswerException(dto.quizId(), auth.getName());
		}
		final var traineeChoices = new ArrayList<Choice>();
		for (var question : quiz.getQuestions()) {
			final var questionDto = dto.getQuestion(question.getId());
			for (var choice : question.getChoices()) {
				if (questionDto.choices().contains(choice.getId())) {
					traineeChoices.add(choice);
				}
			}
		}
		final var test = testRepo.findByIdQuizIdAndIdTraineeName(quiz.getId(), auth.getName()).orElse(new SkillTest(quiz, auth.getName(), traineeChoices));
		test.setSubmittedOn(Instant.now().toEpochMilli());
		test.setChoices(traineeChoices);
		final var saved = testRepo.save(test);
		if (quiz.getIsTrainerNotifiedOfNewTests()) {
			final var users = keycloakAdminApi.getUser(quiz.getAuthorName(), true);

			if (users.size() == 1) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom("noreply@c4-soft.com");
				message.setTo(users.get(0).getEmail());
				message.setSubject("C4 - Quiz: New answer to %s by %s".formatted(quiz.getTitle(), auth.getName()));
				message.setText("%s/tests/%d/%s".formatted(uiUri, quiz.getId(), auth.getName()));
				mailSender.send(message);
			}
		}
		return ResponseEntity.ok(toPreviewDto(saved));
	}

	@DeleteMapping(path = "/{quizId}/{traineeName}")
	@PreAuthorize("authentication.name == #quiz.authorName")
	@Transactional(readOnly = false)
	@Operation(description = "Deletes the answer to given quiz for given trainee. Only the author of a quiz can delete skill-tests.")
	public ResponseEntity<Void> deleteSkillTest(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable(value = "quizId", required = true) Quiz quiz,
			@PathVariable(value = "traineeName", required = true) String traineeName) {
		testRepo.deleteById(new SkillTestPk(quiz.getId(), traineeName));
		return ResponseEntity.accepted().build();
	}

	private SkillTestResultPreviewDto toPreviewDto(SkillTest traineeAnswer) {
		var score = 0;
		var totalChoices = 0;
		final var quiz = quizRepo.findById(traineeAnswer.getId().getQuizId())
				.orElseThrow(() -> new EntityNotFoundException("Unknown quiz: %d".formatted(traineeAnswer.getId().getQuizId())));
		final var testDto = new SkillTestDto(traineeAnswer.getId().getQuizId(), new ArrayList<>());
		for (var question : quiz.getQuestions()) {
			final var traineeQuestionChoices = traineeAnswer.getChoices(question.getId());
			final var questionDto = new SkillTestQuestionDto(question.getId(), new ArrayList<>());
			testDto.questions().add(questionDto);
			for (var choice : question.getChoices()) {
				totalChoices += 1;
				if (traineeQuestionChoices.contains(choice)) {
					questionDto.choices().add(choice.getId());
					score += choice.getIsGood() ? 1 : -1;
				} else {
					score += choice.getIsGood() ? 0 : 1;
				}
			}
		}

		return new SkillTestResultPreviewDto(traineeAnswer.getId().getTraineeName(), totalChoices == 0 ? null : 100.0 * score / totalChoices);
	}

	private SkillTestResultDetailsDto toDetailsDto(SkillTest traineeAnswer) {
		var score = 0;
		var totalChoices = 0;
		final var quiz = quizRepo.findById(traineeAnswer.getId().getQuizId())
				.orElseThrow(() -> new EntityNotFoundException("Unknown quiz: %d".formatted(traineeAnswer.getId().getQuizId())));
		final var testDto = new SkillTestDto(traineeAnswer.getId().getQuizId(), new ArrayList<>());
		for (var question : quiz.getQuestions()) {
			final var traineeQuestionChoices = traineeAnswer.getChoices(question.getId());
			final var questionDto = new SkillTestQuestionDto(question.getId(), new ArrayList<>());
			testDto.questions().add(questionDto);
			for (var choice : question.getChoices()) {
				totalChoices += 1;
				if (traineeQuestionChoices.contains(choice)) {
					questionDto.choices().add(choice.getId());
					score += choice.getIsGood() ? 1 : -1;
				} else {
					score += choice.getIsGood() ? 0 : 1;
				}
			}
		}

		final var users = keycloakAdminApi.getUser(traineeAnswer.getId().getTraineeName(), true);
		final var email = users.size() == 1 ? users.get(0).getEmail() : "";
		final var firstName = users.size() == 1 ? users.get(0).getFirtsName() : "";
		final var lastName = users.size() == 1 ? users.get(0).getLastName() : "";
		return new SkillTestResultDetailsDto(
				testDto,
				traineeAnswer.getId().getTraineeName(),
				firstName,
				lastName,
				email,
				totalChoices == 0 ? null : 100.0 * score / totalChoices);
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	static class InvalidQuizException extends RuntimeException {
		private static final long serialVersionUID = 8816930385638385805L;

		InvalidQuizException(Long quizId) {
			super("Quiz %d doesn't accept answers anymore.".formatted(quizId));
		}
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	static class QuizAlreadyHasAnAnswerException extends RuntimeException {

		private static final long serialVersionUID = 6171083302507116601L;

		QuizAlreadyHasAnAnswerException(Long quizId, String traineeName) {
			super(
					"Quiz %d already has an answer for %s and doesn't accept replay. Ask the trainer to delete the answer before submitting a new one."
							.formatted(quizId, traineeName));
		}
	}
}
