/* (C)2024 */
package com.c4soft.quiz.web;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizAuthentication;
import com.c4soft.quiz.domain.exception.NotADraftException;
import com.c4soft.quiz.domain.jpa.ChoiceRepository;
import com.c4soft.quiz.domain.jpa.QuestionRepository;
import com.c4soft.quiz.domain.jpa.SkillTestRepository;
import com.c4soft.quiz.web.dto.ChoiceUpdateDto;
import com.c4soft.quiz.web.dto.mapping.ChoiceMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/quizzes/{quiz-id}/questions/{question-id}/choices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Quizzes")
public class ChoiceController {
  private final ChoiceRepository choiceRepo;
  private final QuestionRepository questionRepo;
  private final ChoiceMapper choiceMapper;
  private final SkillTestRepository skillTestRepo;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "201",
      headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created choice"))})
  public ResponseEntity<Void> addChoice(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
      @RequestBody @Valid ChoiceUpdateDto dto, QuizAuthentication auth) throws NotADraftException {
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
    final var choice = choiceMapper.update(dto, new Choice());
    question.add(choice);
    choice.setQuestion(question);
    final var created = choiceRepo.save(choice);
    return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
  }

  @PutMapping(path = "/{choice-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> updateChoice(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("choice-id") Long choiceId,
      @RequestBody @Valid ChoiceUpdateDto dto, QuizAuthentication auth) throws NotADraftException {
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
    if (!Objects.equals(dto.label(), choice.getLabel()) && !auth.isModerator()
        && (quiz.getIsPublished() || quiz.getReplacedBy() != null)) {
      throw new NotADraftException(quiz.getId());
    }
    choiceRepo.save(choiceMapper.update(dto, choice));
    return ResponseEntity.accepted().build();
  }

  @DeleteMapping(path = "/{choice-id}")
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> deleteChoice(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("choice-id") Long choiceId,
      QuizAuthentication auth) throws NotADraftException {
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
    questionRepo.save(question);
    choiceRepo.delete(choice);
    return ResponseEntity.accepted().build();
  }
}
