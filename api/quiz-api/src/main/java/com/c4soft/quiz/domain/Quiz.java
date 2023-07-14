package com.c4soft.quiz.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class Quiz {

	public Quiz(String title, String formerName, Question... questions) {
		this.title = title;
		this.formerName = formerName;
		this.questions = new ArrayList<>(questions.length);
		for (var q : questions) {
			this.add(q);
		}
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false)
	private String title;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private String formerName;

	public List<Question> getQuestions() {
		return Collections.unmodifiableList(this.questions);
	}
	
	public Question getQuestion(Long questionId) {
	    if(questionId == null) {
		return null;
	    }
	    return questions.stream().filter(q -> questionId.equals(q.getId())).findAny()
			.orElse(null);
	}

	public Quiz add(Question question) {
		if (question.getQuiz() != null && question.getQuiz() != this) {
			throw new RuntimeException("Question already belongs to another quiz");
		}
		questions.stream().filter(q -> q.getPriority() >= question.getPriority())
				.forEach(q -> q.setPriority(q.getPriority() + 1));
		question.setQuiz(this);
		questions.add(question);
		questions.sort((a, b) -> a.getPriority() - b.getPriority());
		return this;
	}

	public Quiz remove(Question question) {
		if (question.getQuiz() != this) {
			throw new RuntimeException("Question does not belong to this quiz");
		}
		final var isRemoved = questions.remove(question);
		question.setQuiz(null);
		if (isRemoved) {
			questions.stream().filter(q -> q.getPriority() >= question.getPriority())
					.forEach(q -> q.setPriority(q.getPriority() - 1));
		}
		return this;
	}
}
