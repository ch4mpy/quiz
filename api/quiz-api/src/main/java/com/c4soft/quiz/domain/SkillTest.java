package com.c4soft.quiz.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class SkillTest {
    public SkillTest(String traineeName, Collection<Choice> choices) {
	this.submittedOn = Instant.now().toEpochMilli();
	this.choices = new ArrayList<>(choices);
	this.id.traineeName = traineeName;
	final var quizIds = this.choices.stream().map(c -> c.getQuestion().getQuiz().getId()).distinct().toList();
	if(quizIds.size() == 0) {
	    throw new NotAcceptableSkillTestException("A skill test must contain at least one choice.");
	}
	if(quizIds.size() > 1) {
	    throw new NotAcceptableSkillTestException("All choices of test must belong to questions from the same test.");
	}
	this.id.quizId = quizIds.get(0);
    }

    @Setter(AccessLevel.NONE)
    @EmbeddedId
    SkillTestPk id = new SkillTestPk();

    @Column
    private Long submittedOn;

    @ManyToMany
    private List<Choice> choices = new ArrayList<>();

    public List<Choice> getChoices(Long questionId) {
	return choices.stream().filter(c -> c.getQuestion().getId() == questionId).toList();
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
    public static class SkillTestPk {
	    @Setter(AccessLevel.NONE)
	    @Column
	    private Long quizId;

	    @Setter(AccessLevel.NONE)
	    @Column
	    private String traineeName;
    }
}
