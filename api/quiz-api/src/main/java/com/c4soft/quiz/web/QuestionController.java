/* (C)2024 */
package com.c4soft.quiz.web;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.QuizAuthentication;
import com.c4soft.quiz.domain.exception.NotADraftException;
import com.c4soft.quiz.domain.jpa.QuestionRepository;
import com.c4soft.quiz.domain.jpa.SkillTestRepository;
import com.c4soft.quiz.web.dto.QuestionUpdateDto;
import com.c4soft.quiz.web.dto.mapping.QuestionMapper;
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
@RequestMapping(path = "/quizzes/{quiz-id}/questions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Quizzes")
public class QuestionController {
  private final QuestionRepository questionRepo;
  private final QuestionMapper questionMapper;
  private final SkillTestRepository skillTestRepo;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "201",
      headers = @Header(name = HttpHeaders.LOCATION, description = "ID of the created question"))})
  public ResponseEntity<Void> addQuestion(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @RequestBody @Valid QuestionUpdateDto dto, QuizAuthentication auth)
      throws NotADraftException {
    if (quiz.getReplacedBy() != null) {
      throw new NotADraftException(quiz.getId());
    }
    if (quiz.getIsPublished() && !auth.isModerator()) {
      throw new NotADraftException(quiz.getId());
    }

    final var question = questionMapper.update(dto, new Question());
    question.setPriority(quiz.getQuestions().size());
    quiz.add(question);
    final var created = questionRepo.save(question);
    return ResponseEntity.created(URI.create("%d".formatted(created.getId()))).build();
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> updateQuestionsOrder(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @RequestBody @NotEmpty List<Long> questionIds) throws NotADraftException {
    if (quiz.getReplacedBy() != null) {
      throw new NotADraftException(quiz.getId());
    }

    if (quiz.getQuestions().size() != questionIds.size()) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }
    final var quizQuestionIds =
        quiz.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
    for (var id : quizQuestionIds) {
      if (!questionIds.contains(id)) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
      }
    }
    quiz.getQuestions().stream().forEach(q -> q.setPriority(questionIds.indexOf(q.getId())));
    questionRepo.saveAllAndFlush(quiz.getQuestions());
    return ResponseEntity.accepted().build();
  }

  @PutMapping(path = "/{question-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
  public ResponseEntity<Void> updateQuestion(
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("quiz-id") Quiz quiz,
      @Parameter(schema = @Schema(type = "integer")) @PathVariable("question-id") Long questionId,
      @RequestBody @Valid QuestionUpdateDto dto, QuizAuthentication auth) {
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
    question.setFormattedBody(dto.formattedBody());
    questionRepo.save(question);
    return ResponseEntity.accepted().build();
  }

  @DeleteMapping(path = "/{question-id}")
  @PreAuthorize("hasAuthority('trainer') && #quiz.authorName == authentication.name")
  @Transactional(readOnly = false)
  @Operation(responses = {@ApiResponse(responseCode = "202")})
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
    questionRepo.delete(question);
    return ResponseEntity.accepted().build();
  }
}
