package com.c4soft.quiz.web;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.ChoiceRepository;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.QuestionRepository;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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

    @GetMapping
    @Transactional(readOnly = true)
    public List<QuizDto> getQuizList() {
	return quizRepo.findAll().stream().map(QuizController::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('former')")
    @Transactional(readOnly = false)
    @Operation(responses = {
	    @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz")) })
    public ResponseEntity<Void> createQuiz(@RequestBody @Valid QuizUpdateDto dto, Authentication auth) {
	final var created = quizRepo.save(new Quiz(dto.title(), auth.getName()));
	return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
    }

    @GetMapping("/{quiz-id}")
    @Transactional(readOnly = true)
    public QuizDto getQuiz(@PathVariable("quiz-id") Quiz quiz) {
	return toDto(quiz);
    }

    @PutMapping("/{quiz-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> updateQuiz(@PathVariable("quiz-id") Quiz quiz, @RequestBody @Valid QuizUpdateDto dto) {
	quiz.setTitle(dto.title());
	quizRepo.save(quiz);
	return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{quiz-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> deleteQuiz(@PathVariable("quiz-id") Quiz quiz) {
	quizRepo.delete(quiz);
	return ResponseEntity.accepted().build();
    }

    @PostMapping("/{quiz-id}/questions")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    @Operation(responses = {
	    @ApiResponse(headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created question")) })
    public ResponseEntity<Void> addQuestion(@PathVariable("quiz-id") Quiz quiz,
	    @RequestBody @Valid QuestionUpdateDto dto) {
	final var question = new Question(dto.label(), quiz.getQuestions().size(), dto.comment());
	quiz.add(question);
	final var created = questionRepo.save(question);
	return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
    }

    @PutMapping("/{quiz-id}/questions")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> updateQuestionsOrder(@PathVariable("quiz-id") Quiz quiz,
	    @RequestBody @NotEmpty List<Long> questionIds) {
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

    @PutMapping("/{quiz-id}/questions/{question-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> updateQuestion(@PathVariable("quiz-id") Quiz quiz,
	    @PathVariable("question-id") Long questionId, @RequestBody @Valid QuestionUpdateDto dto) {
	final var question = quiz.getQuestion(questionId);
	if(question == null) {
	    return ResponseEntity.notFound().build();
	}
	question.setComment(dto.comment());
	question.setLabel(dto.label());
	questionRepo.save(question);
	return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{quiz-id}/questions/{question-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> deleteQuestion(@PathVariable("quiz-id") Quiz quiz,
	    @PathVariable("question-id") Long questionId) {
	final var question = quiz.getQuestion(questionId);
	if(question == null) {
	    return ResponseEntity.notFound().build();
	}
	quiz.remove(question);
	questionRepo.delete(question);
	quizRepo.save(quiz);
	return ResponseEntity.accepted().build();
    }

    @PostMapping("/{quiz-id}/questions/{question-id}/choices")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> addChoice(@PathVariable("quiz-id") Quiz quiz,
	    @PathVariable("question-id") Long questionId, @RequestBody @Valid ChoiceUpdateDto dto) {
	final var question = quiz.getQuestion(questionId);
	if(question == null) {
	    return ResponseEntity.notFound().build();
	}
	final var choice = new Choice(dto.label(), dto.isGood());
	question.add(choice);
	final var created = choiceRepo.save(choice);
	return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
    }

    @PutMapping("/{quiz-id}/questions/{question-id}/choices/{choice-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> updateChoice(@PathVariable("quiz-id") Quiz quiz,
	    @PathVariable("question-id") Long questionId, @PathVariable("choice-id") Long choiceId,
	    @RequestBody @Valid ChoiceUpdateDto dto) {
	final var question = quiz.getQuestion(questionId);
	if(question == null) {
	    return ResponseEntity.notFound().build();
	}
	final var choice = question.getChoice(choiceId);
	if(choice == null) {
	    return ResponseEntity.notFound().build();
	}
	choice.setIsGood(dto.isGood());
	choice.setLabel(dto.label());
	choiceRepo.save(choice);
	return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{quiz-id}/questions/{question-id}/choices/{choice-id}")
    @PreAuthorize("hasAuthority('moderator') || (hasAuthority('former') && #quiz.formerName == authentication.name)")
    @Transactional(readOnly = false)
    public ResponseEntity<Void> deleteChoice(@PathVariable("quiz-id") Quiz quiz,
	    @PathVariable("question-id") Long questionId, @PathVariable("choice-id") Long choiceId) {
	final var question = quiz.getQuestion(questionId);
	if(question == null) {
	    return ResponseEntity.notFound().build();
	}
	final var choice = question.getChoice(choiceId);
	if(choice == null) {
	    return ResponseEntity.notFound().build();
	}
	question.remove(choice);
	choiceRepo.delete(choice);
	questionRepo.save(question);
	return ResponseEntity.accepted().build();
    }

    private static QuizDto toDto(Quiz q) {
	return q == null ? null
		: new QuizDto(q.getId(), q.getTitle(), q.getQuestions().stream()
			.sorted((a, b) -> a.getPriority() - b.getPriority()).map(QuizController::toDto).toList());
    }

    private static QuestionDto toDto(Question q) {
	return q == null ? null
		: new QuestionDto(q.getQuiz().getId(), q.getId(), q.getLabel(),
			q.getChoices().stream().map(QuizController::toDto).toList(), q.getComment());
    }

    private static ChoiceDto toDto(Choice c) {
	return c == null ? null
		: new ChoiceDto(c.getQuestion().getQuiz().getId(), c.getQuestion().getId(), c.getId(), c.getLabel(),
			c.getIsGood());
    }
}
