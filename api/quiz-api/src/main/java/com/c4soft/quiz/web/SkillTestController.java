package com.c4soft.quiz.web;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.QuizRepository;
import com.c4soft.quiz.domain.SkillTest;
import com.c4soft.quiz.domain.SkillTestRepository;
import com.c4soft.quiz.web.dto.SkillTestDto;
import com.c4soft.quiz.web.dto.SkillTestQuestionDto;
import com.c4soft.quiz.web.dto.SkillTestResultDto;

import io.swagger.v3.oas.annotations.Operation;
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

	@GetMapping
	@Transactional(readOnly = true)
	@Operation(description = "Returns the answers to a quiz, by default for all trainees over the last 2 weeks")
	public List<SkillTestResultDto> getTestList(
			@RequestParam(value = "quizId", required = true) Long quizId,
			@RequestParam(value = "since", required = false) Optional<Long> since,
			@RequestParam(value = "until", required = false) Optional<Long> until,
			@RequestParam(value = "traineeName", required = false) Optional<String> traineeName) {
		final var resolvedSince = since.orElse(Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli());
		final var tests = traineeName.isEmpty()
				? testRepo.findByIdQuizId(quizId).stream()
				: testRepo.findAll(SkillTestRepository.spec(quizId, resolvedSince, until, traineeName))
						.stream();
		return tests.map(this::toDto).toList();
	}

	@PutMapping
	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> submitQuizChoices(@RequestBody @Valid SkillTestDto dto, Authentication auth) {
		final var quiz =
				quizRepo.findById(dto.quizId()).orElseThrow(() -> new EntityNotFoundException("Quiz %d was removed from the database".formatted(dto.quizId())));
		final var traineeChoices = new ArrayList<Choice>();
		for (var question : quiz.getQuestions()) {
			final var questionDto = dto.getQuestion(question.getId());
			for (var choice : question.getChoices()) {
				if (questionDto.choices().contains(choice.getId())) {
					traineeChoices.add(choice);
				}
			}
		}
		final var test = testRepo.findByIdQuizIdAndIdTraineeName(quiz.getId(), auth.getName()).orElse(new SkillTest(auth.getName(), traineeChoices));
		test.setSubmittedOn(Instant.now().toEpochMilli());
		test.setChoices(traineeChoices);
		testRepo.save(test);
		return ResponseEntity.accepted().build();
	}

	private SkillTestResultDto toDto(SkillTest traineeAnswer) {
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
		return new SkillTestResultDto(testDto, totalChoices == 0 ? null : 100.0 * score / totalChoices);
	}
}
