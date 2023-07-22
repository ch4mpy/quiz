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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class Question {
	public Question(String label, Integer priority, String comment, Choice... choices) {
		this.label = label;
		this.priority = priority;
		this.comment = comment;
		this.choices = new ArrayList<>(choices.length);
		for(var c : choices) {
			this.add(c);
		}
	}
	
	public Question(Question other) {
		this.comment = other.comment;
		this.label = other.label;
		this.priority = other.priority;
		this.choices = new ArrayList<>(other.choices.size());
		for(var c : other.choices) {
			final var choice = new Choice(c);
			this.add(choice);
		}
	}

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "quiz_id", updatable = false, nullable = false)
	private Quiz quiz;
	
	@Column
	private String label;
	
	@Column(nullable = false, updatable = true)
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
	    if(choiceId == null) {
		return null;
	    }
	    return choices.stream().filter(q -> choiceId.equals(q.getId())).findAny()
			.orElse(null);
	}
	
	public Question add(Choice choice) {
		if(choice.getQuestion() != null && choice.getQuestion() != this) {
			throw new RuntimeException("Choice already belongs to another question");
		}
		choice.setQuestion(this);
		choices.add(choice);
		return this;
	}
	
	public Question remove(Choice choice) {
		if(choice.getQuestion() != this) {
			throw new RuntimeException("Choice does not belongs to another question");
		}
		choices.remove(choice);
		choice.setQuestion(null);
		return this;
	}

}
