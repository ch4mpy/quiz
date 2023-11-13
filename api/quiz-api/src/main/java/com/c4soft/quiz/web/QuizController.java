package com.c4soft.quiz.web;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.ChoiceRepository;
import com.c4soft.quiz.domain.DraftAlreadyExistsException;
import com.c4soft.quiz.domain.NotADraftException;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.QuestionRepository;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizAuthentication;
import com.c4soft.quiz.domain.QuizRejectionDto;
import com.c4soft.quiz.domain.QuizRepository;
import com.c4soft.quiz.domain.SkillTestRepository;
import com.c4soft.quiz.web.dto.ChoiceDto;
import com.c4soft.quiz.web.dto.ChoiceUpdateDto;
import com.c4soft.quiz.web.dto.QuestionDto;
import com.c4soft.quiz.web.dto.QuestionUpdateDto;
import com.c4soft.quiz.web.dto.QuizDto;
import com.c4soft.quiz.web.dto.QuizUpdateDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/quizzes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Quizzes")
public class QuizController {
	private final QuizRepository quizRepo;
	private final QuestionRepository questionRepo;
	private final ChoiceRepository choiceRepo;
	private final SkillTestRepository skillTestRepo;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public List<QuizDto> getQuizList(
			@RequestParam(name = "authorLike", required = false) Optional<String> author,
			@RequestParam(name = "titleLike", required = false) Optional<String> title) {
		final var spec = QuizRepository.searchSpec(author, title);
		final var quizzes = quizRepo.findAll(spec);
		return quizzes.stream().map(QuizController::toDto).toList();
	}

	@GetMapping(path = "/submitted", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('moderator')")
	@Transactional(readOnly = true)
	public List<QuizDto> getSubmittedQuizzes() {
		final var quizzes = quizRepo.findByIsSubmitted(true);
		return quizzes.stream().map(QuizController::toDto).toList();
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer')")
	@Transactional(readOnly = false)
	@Operation(responses = { @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz")) })
	public ResponseEntity<Void> createQuiz(@RequestBody @Valid QuizUpdateDto dto, QuizAuthentication auth) {
		final var quiz = new Quiz(dto.title(), auth.getName());
		final var created = quizRepo.save(quiz);
		return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
	}

	@GetMapping(path = "/{quiz-id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public QuizDto getQuiz(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz) {
		return toDto(quiz);
	}

	@PutMapping(path = "/{quiz-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuiz(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@RequestBody @Valid QuizUpdateDto dto,
			QuizAuthentication auth) {
		updateTitle(quiz, dto.title(), auth);
		quiz.setIsChoicesShuffled(dto.isChoicesShuffled());
		quiz.setIsReplayEnabled(dto.isReplayEnabled());
		quiz.setIsPerQuestionResult(dto.isPerQuestionResult());
		quiz.setIsTrainerNotifiedOfNewTests(dto.isTrainerNotifiedOfNewTests());
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	void updateTitle(Quiz quiz, String title, QuizAuthentication auth) {
		if (Objects.equals(quiz.getTitle(), title)) {
			return;
		}
		if (quiz.getIsPublished() && !auth.isModerator()) {
			throw new NotADraftException(quiz.getId());
		}
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}
		quiz.setTitle(title);
	}

	@PostMapping(path = "/{quiz-id}/duplicate")
	@PreAuthorize("hasAuthority('trainer')")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> createCopy(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz, Authentication auth) {
		final var draft = new Quiz(quiz, auth.getName());
		final var created = quizRepo.save(draft);
		return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
	}

	@PostMapping(path = "/{quiz-id}/draft")
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> createDraft(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz, Authentication auth) {
		if (quiz.getDraft() != null) {
			throw new DraftAlreadyExistsException(quiz.getId());
		}
		if (!quiz.getIsPublished()) {
			throw new DraftAlreadyExistsException(quiz.getId());
		}
		final var draft = new Quiz(quiz, auth.getName());
		draft.setReplaces(quiz);
		final var created = quizRepo.save(draft);
		quiz.setDraft(created);
		quizRepo.save(quiz);
		return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
	}

	@PutMapping(path = "/{quiz-id}/submit")
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> submitDraft(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz, QuizAuthentication auth) {
		if (auth.isModerator()) {
			return publishDraft(quiz, auth);
		}
		quiz.setIsSubmitted(!auth.isModerator());
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@PutMapping(path = "/{quiz-id}/publish")
	@PreAuthorize("hasAuthority('moderator')")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> publishDraft(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz, Authentication auth) {
		quiz.setIsSubmitted(false);
		quiz.setIsPublished(true);
		quiz.setModeratorComment(null);
		quiz.setModeratedBy(auth.getName());
		if (quiz.getReplaces() != null) {
			final var replaced = quiz.getReplaces();
			replaced.setReplacedBy(quiz);
			replaced.setIsPublished(false);
			quiz.setReplaces(null);
			quizRepo.save(replaced);
		}

		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@PutMapping(path = "/{quiz-id}/reject", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('moderator')")
	@Transactional(readOnly = false)
	public
			ResponseEntity<Void>
			rejectDraft(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz, @Valid QuizRejectionDto dto, Authentication auth) {
		quiz.setIsSubmitted(false);
		quiz.setIsPublished(false);
		quiz.setModeratorComment(dto.message());
		quiz.setModeratedBy(auth.getName());
		quizRepo.save(quiz);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping(path = "/{quiz-id}")
	@PreAuthorize("hasAuthority('moderator') || (hasAuthority('trainer') && #quiz.authorName == authentication.name)")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteQuiz(@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz) {
		final var draft = quiz.getDraft();
		if (draft != null) {
			draft.setReplaces(null);
			quiz.setDraft(null);
			quizRepo.save(draft);
		}

		final var replacedBy = quiz.getReplacedBy();
		if (replacedBy != null) {
			replacedBy.setReplaces(null);
			quiz.setReplacedBy(null);
			quizRepo.save(replacedBy);
		}

		final var replaces = quiz.getReplaces();
		if (replaces != null) {
			if (replaces.getReplacedBy() != null && Objects.equals(replaces.getReplacedBy().getId(), quiz.getId())) {
				replaces.setReplacedBy(null);
			}
			if (replaces.getDraft().getId() != null && Objects.equals(replaces.getDraft().getId(), quiz.getId())) {
				replaces.setDraft(null);
			}
			quiz.setReplaces(null);
			quizRepo.save(replaces);
		}

		skillTestRepo.deleteByIdQuizId(quiz.getId());
		questionRepo.deleteAll(quiz.getQuestions());
		quiz.getQuestions().clear();

		quizRepo.delete(quiz);
		return ResponseEntity.accepted().build();
	}

	@PostMapping(path = "/{quiz-id}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	@Operation(responses = { @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created question")) })
	public ResponseEntity<Void> addQuestion(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@RequestBody @Valid QuestionUpdateDto dto,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}
		if (quiz.getIsPublished() && !auth.isModerator()) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = new Question(dto.label(), quiz.getQuestions().size(), dto.comment());
		quiz.add(question);
		final var created = questionRepo.save(question);
		return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
	}

	@PutMapping(path = "/{quiz-id}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuestionsOrder(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@RequestBody @NotEmpty List<Long> questionIds) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}

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
		quiz = quizRepo.saveAndFlush(quiz);
		return ResponseEntity.accepted().build();
	}

	@PutMapping(path = "/{quiz-id}/questions/{question-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateQuestion(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
			@RequestBody @Valid QuestionUpdateDto dto,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}
		if (quiz.getIsPublished() && !auth.isModerator()) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = quiz.getQuestion(questionId);
		if (question == null) {
			return ResponseEntity.notFound().build();
		}
		question.setComment(dto.comment());
		question.setLabel(dto.label());
		questionRepo.save(question);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping(path = "/{quiz-id}/questions/{question-id}")
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteQuestion(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = quiz.getQuestion(questionId);
		if (question == null) {
			return ResponseEntity.notFound().build();
		}

		final var tests = skillTestRepo.findByIdQuizId(quiz.getId());
		tests.forEach(t -> {
			final var filtered = t.getChoices().stream().filter(c -> {
				return c.getQuestion().getId() != questionId;
			}).toList();
			t.setChoices(filtered);
		});
		skillTestRepo.saveAll(tests);

		quiz.remove(question);
		quizRepo.save(quiz);
		choiceRepo.deleteAll(question.getChoices());
		questionRepo.delete(question);
		return ResponseEntity.accepted().build();
	}

	@PostMapping(path = "/{quiz-id}/questions/{question-id}/choices", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> addChoice(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
			@RequestBody @Valid ChoiceUpdateDto dto,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}
		if (quiz.getIsPublished() && !auth.isModerator()) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = quiz.getQuestion(questionId);
		if (question == null) {
			return ResponseEntity.notFound().build();
		}
		final var choice = new Choice(dto.label(), dto.isGood());
		question.add(choice);
		final var created = choiceRepo.save(choice);
		return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
	}

	@PutMapping(path = "/{quiz-id}/questions/{question-id}/choices/{choice-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> updateChoice(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("choice-id") Long choiceId,
			@RequestBody @Valid ChoiceUpdateDto dto,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}
		if (quiz.getIsPublished() && !auth.isModerator()) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = quiz.getQuestion(questionId);
		if (question == null) {
			return ResponseEntity.notFound().build();
		}
		final var choice = question.getChoice(choiceId);
		if (choice == null) {
			return ResponseEntity.notFound().build();
		}
		if (!Objects.equals(dto.label(), choice.getLabel()) && (quiz.getIsPublished() || quiz.getReplacedBy() != null)) {
			throw new NotADraftException(quiz.getId());
		}
		choice.setIsGood(dto.isGood());
		choice.setLabel(dto.label());
		choiceRepo.save(choice);
		return ResponseEntity.accepted().build();
	}

	@DeleteMapping(path = "/{quiz-id}/questions/{question-id}/choices/{choice-id}")
	@PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
	@Transactional(readOnly = false)
	public ResponseEntity<Void> deleteChoice(
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
			@Parameter(schema = @Schema(type = "integer")) @PathVariable("choice-id") Long choiceId,
			QuizAuthentication auth) {
		if (quiz.getReplacedBy() != null) {
			throw new NotADraftException(quiz.getId());
		}

		final var question = quiz.getQuestion(questionId);
		if (question == null) {
			return ResponseEntity.notFound().build();
		}
		final var choice = question.getChoice(choiceId);
		if (choice == null) {
			return ResponseEntity.notFound().build();
		}

		final var tests = skillTestRepo.findByIdQuizId(quiz.getId());
		tests.forEach(t -> {
			final var filtered = t.getChoices().stream().filter(c -> {
				return c.getId() != choiceId;
			}).toList();
			t.setChoices(filtered);
		});
		skillTestRepo.saveAll(tests);

		question.remove(choice);
		choiceRepo.delete(choice);
		questionRepo.save(question);
		return ResponseEntity.accepted().build();
	}

	private static QuizDto toDto(Quiz q) {
		return q == null
				? null
				: new QuizDto(
						q.getId(),
						q.getTitle(),
						q.getQuestions().stream().sorted((a, b) -> a.getPriority() - b.getPriority()).map(QuizController::toDto).toList(),
						q.getAuthorName(),
						q.getIsPublished(),
						q.getIsSubmitted(),
						q.getReplacedBy() != null,
						q.getIsChoicesShuffled(),
						q.getIsReplayEnabled(),
						q.getIsPerQuestionResult(),
						q.getIsTrainerNotifiedOfNewTests(),
						q.getModeratorComment(),
						q.getDraft() == null ? null : q.getDraft().getId(),
						q.getReplaces() == null ? null : q.getReplaces().getId());
	}

	private static QuestionDto toDto(Question q) {
		return q == null
				? null
				: new QuestionDto(q.getQuiz().getId(), q.getId(), q.getLabel(), q.getChoices().stream().map(QuizController::toDto).toList(), q.getComment());
	}

	private static ChoiceDto toDto(Choice c) {
		return c == null ? null : new ChoiceDto(c.getQuestion().getQuiz().getId(), c.getQuestion().getId(), c.getId(), c.getLabel(), c.getIsGood());
	}
}
