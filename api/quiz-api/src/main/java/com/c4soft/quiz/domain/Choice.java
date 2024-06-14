/* (C)2024 */
package com.c4soft.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Choice {
  public Choice(String label, Boolean isGood) {
    this.label = label;
    this.isGood = isGood;
  }

  public Choice(Choice other) {
    this.label = other.label;
    this.isGood = other.isGood;
  }

  @Id
  @GeneratedValue
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "question_id", updatable = false, nullable = false)
  private Question question;

  @Column
  @ToString.Include
  private String label;

  @Column
  @ToString.Include
  private Boolean isGood;
}
