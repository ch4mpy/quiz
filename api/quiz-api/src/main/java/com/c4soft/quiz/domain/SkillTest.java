/* (C)2024 */
package com.c4soft.quiz.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class SkillTest {
  public SkillTest(Quiz quiz, String traineeName, Collection<Choice> choices) {
    this.submittedOn = Instant.now().toEpochMilli();
    this.choices = new ArrayList<>(choices);
    this.id.traineeName = traineeName;
    this.choices.forEach(c -> {
      if (c.getQuestion() == null || c.getQuestion().getQuiz() == null
          || !Objects.equals(c.getQuestion().getQuiz().getId(), quiz.getId())) {
        throw new NotAcceptableSkillTestException(
            "All choices must target the same quiz (%s)".formatted(quiz.getTitle()));
      }
    });
    this.id.quizId = quiz.getId();
  }

  @Setter(AccessLevel.NONE)
  @EmbeddedId
  private final SkillTestPk id = new SkillTestPk();

  @Column
  private Long submittedOn;

  @ManyToMany
  private List<Choice> choices = new ArrayList<>();

  public List<Choice> getChoices(Long questionId) {
    return choices.stream().filter(c -> Objects.equals(c.getQuestion().getId(), questionId))
        .toList();
  }

  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  static class NotAcceptableSkillTestException extends RuntimeException {
    private static final long serialVersionUID = -6754084213295394103L;

    public NotAcceptableSkillTestException(String message) {
      super(message);
    }
  }

  @Embeddable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SkillTestPk {
    @Setter(AccessLevel.NONE)
    @Column
    private Long quizId;

    @Setter(AccessLevel.NONE)
    @Column
    private String traineeName;
  }
}
