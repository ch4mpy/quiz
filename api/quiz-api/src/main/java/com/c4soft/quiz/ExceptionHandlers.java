/* (C)2024 */
package com.c4soft.quiz;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.c4soft.quiz.domain.exception.DraftAlreadyExistsException;
import com.c4soft.quiz.domain.exception.InvalidQuizException;
import com.c4soft.quiz.domain.exception.NotADraftException;
import com.c4soft.quiz.domain.exception.QuizAlreadyHasAnAnswerException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ExceptionHandlers {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ApiResponse(responseCode = "422",
      content = {@Content(schema = @Schema(implementation = ValidationProblemDetail.class))})
  public ResponseEntity<ValidationProblemDetail> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    final var detail = new ValidationProblemDetail(ex.getMessage(), ex.getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getCode)));
    return ResponseEntity.status(detail.getStatus()).body(detail);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ApiResponse(responseCode = "422",
      content = {@Content(schema = @Schema(implementation = ValidationProblemDetail.class))})
  public ResponseEntity<ValidationProblemDetail> handleConstraintViolation(
      ConstraintViolationException ex) {
    final var problem = new ValidationProblemDetail(ex.getMessage(),
        ex.getConstraintViolations().stream().collect(Collectors
            .toMap(cv -> cv.getPropertyPath().toString(), ConstraintViolation::getMessage)));
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ApiResponse(responseCode = "422",
      content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    final var detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    return ResponseEntity.status(detail.getStatus()).body(detail);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ApiResponse(responseCode = "404",
      content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex) {
    final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  @ExceptionHandler(MissingPathVariableException.class)
  @ApiResponse(responseCode = "404",
      content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleMissingPathVariableException(
      MissingPathVariableException ex) {
    final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  @ExceptionHandler({InvalidQuizException.class, QuizAlreadyHasAnAnswerException.class,
      DraftAlreadyExistsException.class, NotADraftException.class})
  @ApiResponse(responseCode = "409",
      content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
  public ResponseEntity<ProblemDetail> handleConflicts(NotADraftException ex) {
    final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  public static class ValidationProblemDetail extends ProblemDetail {
    public static final URI TYPE = URI.create("https://quiz.c4-soft.com/problems/validation");
    public static final String INVALID_FIELDS_PROPERTY = "invalidFields";

    public ValidationProblemDetail(String message, Map<String, String> invalidFields) {
      super(ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, message));
      super.setType(TYPE);
      super.setProperty(INVALID_FIELDS_PROPERTY, invalidFields);
    }

    @SuppressWarnings("unchecked")
    @JsonSerialize
    Map<String, String> getInvalidFields() {
      return (Map<String, String>) super.getProperties().get(INVALID_FIELDS_PROPERTY);
    }
  }
}
