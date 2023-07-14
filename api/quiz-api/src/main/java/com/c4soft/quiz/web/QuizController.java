package com.c4soft.quiz.web;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.ChoiceRepository;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.QuestionRepository;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/quiz", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Tag(name = "Quiz")
public class QuizController {
	private final QuizRepository quizRepo;
	private final QuestionRepository questionRepo;
	private final ChoiceRepository choiceRepo;

	@GetMapping("/")
	@Transactional(readOnly = true)
	public List<QuizDto> getQuizList() {
		return quizRepo.findAll().stream().map(QuizController::toDto).toList();
	}

	@PostMapping("/")
	@PreAuthorize("hasAuthority('former')")
	@Transactional(readOnly = false)
	@Operation(responses = { @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz")) })
	public ResponseEntity<Void> createQuiz(@RequestBody @Valid QuizUpdateDto dto, Authentication auth) {
		final var created = quizRepo.save(new Quiz(null, dto.title(), List.of(), auth.getName()));
		return ResponseEntity.accepted().location(URI.create("%d".formatted(created.getId()))).build();
	}

	@GetMapping("/{quiz-id}")
	@Transactional(readOnly = true)
	public QuizDto getQuiz(@PathVariable("quiz-id") Quiz quiz) {
		return toDto(quiz);
	}

	@PutMapping("/{quiz-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuiz(@PathVariable("quiz-id") Quiz quiz, @RequestBody @Valid QuizUpdateDto dto, Authentication auth) {
		quiz.setTitle(dto.title());
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping("/{quiz-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteQuiz(@PathVariable("quiz-id") Long quizId) {
		quizRepo.deleteById(quizId);
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/{quiz-id}/questions")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	@Operation(responses = { @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created question")) })
	public ResponseEntity<Void> addQuestion(@PathVariable("quiz-id") Quiz quiz, @RequestBody @Valid QuestionUpdateDto dto, Authentication auth) {
		final var maxPriority = quiz.getQuestions().stream().map(Question::getPriority).max(Integer::compare).orElse(0);
		final var priority = dto.order() > maxPriority + 1 ? maxPriority + 1 : dto.order();
		final var question = new Question(null, quiz, dto.label(), priority, List.of(), dto.comment());
		quiz.getQuestions().stream().filter(q -> q.getPriority() >= priority).forEach(q -> q.setPriority(q.getPriority() + 1));
		quiz.getQuestions().add(question);
		final var created = questionRepo.save(question);
		return ResponseEntity.accepted().location(URI.create("%d".formatted(created.getId()))).build();
	}

	@PutMapping("/{quiz-id}/questions")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuestionsOrder(@PathVariable("quiz-id") Quiz quiz, @RequestBody @NotEmpty List<Long> questionIds, Authentication auth) {
		if (quiz.getQuestions().size() != questionIds.size()) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
		}
		final var quizQuestionIds = quiz.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
		for (var id : quizQuestionIds) {
			if (!questionIds.contains(id)) {
				return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
			}
		}
		quiz.getQuestions().stream().forEach(q -> q.setPriority(questionIds.indexOf(q.getId())));
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@PutMapping("/{quiz-id}/questions/{question-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuestion(
			@PathVariable("quiz-id") Quiz quiz,
			@PathVariable("question-id") Question question,
			@RequestBody @Valid QuestionUpdateDto dto,
			Authentication auth) {
		if (quiz.getQuestions().contains(question)) {
			return ResponseEntity.notFound().build();
		}
		question.setComment(dto.comment());
		questionRepo.save(question);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping("/{quiz-id}/questions/{question-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteQuestion(@PathVariable("quiz-id") Quiz quiz, @PathVariable("question-id") Question question, Authentication auth) {
		if (quiz.getQuestions().contains(question)) {
			return ResponseEntity.notFound().build();
		}
		quiz.getQuestions().remove(question);
		quiz.getQuestions().stream().filter(q -> q.getPriority() > question.getPriority()).forEach(q -> q.setPriority(q.getPriority() - 1));
		questionRepo.delete(question);
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/{quiz-id}/questions/{question-id}/choices")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> addChoice(
			@PathVariable("quiz-id") Quiz quiz,
			@PathVariable("question-id") Question question,
			@RequestBody @Valid ChoiceUpdateDto dto,
			Authentication auth) {
		if (quiz.getQuestions().contains(question)) {
			return ResponseEntity.notFound().build();
		}
		final var choice = new Choice(null, question, dto.label(), dto.isGood());
		question.getChoices().add(choice);
		final var created = choiceRepo.save(choice);
		return ResponseEntity.accepted().location(URI.create("%d".formatted(created.getId()))).build();
	}

	@PutMapping("/{quiz-id}/questions/{question-id}/choices/{choice-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateChoice(
			@PathVariable("quiz-id") Quiz quiz,
			@PathVariable("question-id") Question question,
			@PathVariable("choice-id") Choice choice,
			@RequestBody @Valid ChoiceUpdateDto dto,
			Authentication auth) {
		if (quiz.getQuestions().contains(question)) {
			return ResponseEntity.notFound().build();
		}
		choice.setIsGood(dto.isGood());
		choice.setLabel(dto.label());
		choiceRepo.save(choice);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping("/{quiz-id}/questions/{question-id}/choices/{choice-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == auth.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteChoice(
			@PathVariable("quiz-id") Quiz quiz,
			@PathVariable("question-id") Question question,
			@PathVariable("choice-id") Choice choice,
			@RequestBody @Valid ChoiceUpdateDto dto,
			Authentication auth) {
		if (quiz.getQuestions().contains(question) || !question.getChoices().contains(choice)) {
			return ResponseEntity.notFound().build();
		}
		question.getChoices().remove(choice);
		choiceRepo.delete(choice);
		questionRepo.save(question);
		return ResponseEntity.accepted().build();
	}

	private static QuizDto toDto(Quiz q) {
		return q == null ? null : new QuizDto(q.getId(), q.getTitle(), q.getQuestions().stream().map(QuizController::toDto).toList());
	}

	private static QuestionDto toDto(Question q) {
		return q == null ? null : new QuestionDto(q.getQuiz().getId(), q.getId(), q.getPriority(), q.getChoices().stream().map(QuizController::toDto).toList(), q.getComment());
	}

	private static ChoiceDto toDto(Choice c) {
		return c == null ? null : new ChoiceDto(c.getQuestion().getQuiz().getId(), c.getQuestion().getId(), c.getId(), c.getLabel(), c.getIsGood());
	}

	public static record QuizUpdateDto(@NotEmpty String title) {
	}

	public static record QuizDto(@NotNull Long id, @NotEmpty String title, @NotNull List<QuestionDto> questionIds) {
	}

	public static record QuestionUpdateDto(@NotEmpty String label, @NotNull Integer order, @NotNull String comment) {
	}

	public static record QuestionDto(@NotNull Long quizId, @NotNull Long questionId, @NotNull Integer order, List<ChoiceDto> choices, @NotNull String comment) {
	}

	public static record ChoiceUpdateDto(@NotEmpty String label, boolean isGood) {
	}

	public static record ChoiceDto(@NotNull Long quizId, @NotNull Long questionId, @NotNull Long choiceId, @NotEmpty String label, boolean isGood) {
	}
}
