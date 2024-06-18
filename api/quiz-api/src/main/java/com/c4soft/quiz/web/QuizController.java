/* (C)2024 */
package com.c4soft.quiz.web;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
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
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizAuthentication;
import com.c4soft.quiz.domain.QuizRejectionDto;
import com.c4soft.quiz.domain.exception.DraftAlreadyExistsException;
import com.c4soft.quiz.domain.exception.NotADraftException;
import com.c4soft.quiz.domain.jpa.QuizRepository;
import com.c4soft.quiz.domain.jpa.SkillTestRepository;
import com.c4soft.quiz.web.dto.QuizDto;
import com.c4soft.quiz.web.dto.QuizUpdateDto;
import com.c4soft.quiz.web.dto.mapping.QuizMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/quizzes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Quizzes")
public class QuizController {
  private final QuizRepository quizRepo;
  private final SkillTestRepository skillTestRepo;
  private final QuizMapper quizMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  public List<QuizDto> getQuizList(
      @RequestParam(name = "authorLike", required = false) Optional<String> author,
      @RequestParam(name = "titleLike", required = false) Optional<String> title) {
    final var spec = QuizRepository.searchSpec(author, title);
    final var quizzes = quizRepo.findAll(spec);
    return quizzes.stream().map(quizMapper::toDto).toList();
  }

  @GetMapping(path = "/submitted", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('moderator')")
  @Transactional(readOnly = true)
  public List<QuizDto> getSubmittedQuizzes() {
    final var quizzes = quizRepo.findByIsSubmitted(true);
    return quizzes.stream().map(quizMapper::toDto).toList();
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer')")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "201",
      headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz"))})
  public ResponseEntity<Void> createQuiz(@RequestBody @Valid QuizUpdateDto dto,
      QuizAuthentication auth) {
    final var quiz = quizMapper.update(dto, new Quiz());
    quiz.setAuthorName(auth.getName());
    final var created = quizRepo.save(quiz);
    return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
  }

  @GetMapping(path = "/{quiz-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  public QuizDto getQuiz(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz) {
    return quizMapper.toDto(quiz);
  }

  @PutMapping(path = "/{quiz-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> updateQuiz(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @RequestBody @Valid QuizUpdateDto dto, QuizAuthentication auth) throws NotADraftException {
    checkCanUpdate(quiz, auth);
    quizRepo.save(quizMapper.update(dto, quiz));
    return ResponseEntity.accepted().build();
  }

  void checkCanUpdate(Quiz quiz, QuizAuthentication auth) throws NotADraftException {
    if (quiz.getIsPublished() && !auth.isModerator()) {
      throw new NotADraftException(quiz.getId());
    }
    if (quiz.getReplacedBy() != null) {
      throw new NotADraftException(quiz.getId());
    }
  }

  @PostMapping(path = "/{quiz-id}/duplicate")
  @PreAuthorize("hasAuthority('trainer')")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "201",
      headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz"))})
  public ResponseEntity<Void> createCopy(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      Authentication auth) {
    final var draft = new Quiz(quiz, auth.getName());
    final var created = quizRepo.save(draft);
    return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
  }

  @PostMapping(path = "/{quiz-id}/draft")
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "201",
      headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created quiz"))})
  public ResponseEntity<Void> createDraft(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      Authentication auth) throws DraftAlreadyExistsException {
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
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> submitDraft(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      QuizAuthentication auth) {
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
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> publishDraft(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      Authentication auth) {
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
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> rejectDraft(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @Valid QuizRejectionDto dto, Authentication auth) {
    quiz.setIsSubmitted(false);
    quiz.setIsPublished(false);
    quiz.setModeratorComment(dto.message());
    quiz.setModeratedBy(auth.getName());
    quizRepo.save(quiz);
    return ResponseEntity.accepted().build();
  }

  @DeleteMapping(path = "/{quiz-id}")
  @PreAuthorize("hasAuthority('moderator') || (hasAuthority('trainer') && #quiz.authorName =="
      + " authentication.name)")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> deleteQuiz(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz) {
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
      if (replaces.getReplacedBy() != null
          && Objects.equals(replaces.getReplacedBy().getId(), quiz.getId())) {
        replaces.setReplacedBy(null);
      }
      if (replaces.getDraft().getId() != null
          && Objects.equals(replaces.getDraft().getId(), quiz.getId())) {
        replaces.setDraft(null);
      }
      quiz.setReplaces(null);
      quizRepo.save(replaces);
    }

    skillTestRepo.deleteByIdQuizId(quiz.getId());
    quiz.getQuestions().clear();

    quizRepo.delete(quiz);
    return ResponseEntity.accepted().build();
  }
}
