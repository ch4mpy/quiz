package com.c4soft.quiz.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class Quiz {

	public Quiz(String title, String authorName, Question... questions) {
		this.title = title;
		this.authorName = authorName;
		this.questions = new ArrayList<>(questions.length);
		for (var q : questions) {
			this.add(q);
		}
	}

	public Quiz(Quiz other, String authorName) {
		this.authorName = authorName;
		this.questions = new ArrayList<>(other.questions.size());
		for (var q : other.questions) {
			final var question = new Question(q);
			this.add(question);
		}
		this.isPublished = false;
		this.moderatedBy = null;
		this.title = other.title;
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String title;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private String authorName;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isSubmitted = false;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isPublished = false;

	@OneToOne(optional = true)
	private Quiz draft;

	@OneToOne(optional = true)
	private Quiz replaces;

	@OneToOne(optional = true)
	private Quiz replacedBy;

	@Column()
	private String moderatorComment;

	@Column()
	private String moderatedBy;

	@Column(nullable = false, columnDefinition = "boolean default true")
	private Boolean isChoicesShuffled = true;

	@Column(nullable = false, columnDefinition = "boolean default true")
	private Boolean isReplayEnabled = true;
	
	@Column(nullable = false, columnDefinition = "boolean default true")
	private Boolean isPerQuestionResult = true;
	
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isTrainerNotifiedOfNewTests = false;

	@Setter(AccessLevel.NONE)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SkillTest> skillTests = new ArrayList<>();
	
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
