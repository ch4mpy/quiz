/* (C)2024 */
package com.c4soft.quiz.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
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
public class Question {
  public Question(String label, String formattedBody, Integer priority, String comment,
      Choice... choices) {
    this.label = label;
    this.formattedBody = formattedBody;
    this.priority = priority;
    this.comment = comment;
    this.choices = new ArrayList<>(choices.length);
    for (var c : choices) {
      this.add(c);
    }
  }

  public Question(Question other) {
    this.comment = other.comment;
    this.label = other.label;
    this.formattedBody = other.formattedBody;
    this.priority = other.priority;
    this.choices = new ArrayList<>(other.choices.size());
    for (var c : other.choices) {
      final var choice = new Choice(c);
      this.add(choice);
    }
  }

  @Id
  @GeneratedValue
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "quiz_id", updatable = false, nullable = false)
  private Quiz quiz;

  @Column
  @ToString.Include
  private String label;

  @Column(length = 2047)
  private String formattedBody;

  @Column(nullable = false, updatable = true)
  @ToString.Include
  private Integer priority;

  @Setter(AccessLevel.NONE)
  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Choice> choices = new ArrayList<>();

  @Column(length = 2048)
  private String comment;

  public List<Choice> getChoices() {
    return Collections.unmodifiableList(choices);
  }

  public Choice getChoice(Long choiceId) {
    if (choiceId == null) {
      return null;
    }
    return choices.stream().filter(q -> choiceId.equals(q.getId())).findAny().orElse(null);
  }

  public Question add(Choice choice) {
    if (choice.getQuestion() != null && choice.getQuestion() != this) {
      throw new RuntimeException("Choice already belongs to another question");
    }
    choice.setQuestion(this);
    choices.add(choice);
    return this;
  }

  public Question remove(Choice choice) {
    if (choice.getQuestion() != this) {
      throw new RuntimeException("Choice does not belongs to another question");
    }
    choices.remove(choice);
    choice.setQuestion(null);
    return this;
  }
}
