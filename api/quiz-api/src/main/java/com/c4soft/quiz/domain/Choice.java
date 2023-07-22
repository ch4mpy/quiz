package com.c4soft.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "question_id", updatable = false, nullable = false)
	private Question question;
	
	@Column
	private String label;
	
	@Column
	private Boolean isGood;

}
