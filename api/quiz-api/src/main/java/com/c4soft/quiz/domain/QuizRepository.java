package com.c4soft.quiz.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.core.context.SecurityContextHolder;

public interface QuizRepository extends JpaRepository<Quiz, Long>, JpaSpecificationExecutor<Quiz> {

	Optional<Quiz> findByReplacesId(Long replacedQuizId);

	List<Quiz> findByIsSubmitted(boolean isSubmitted);

	static Specification<Quiz> searchSpec(Optional<String> authorName, Optional<String> quizTitle) {
		final var currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
		var spec = Specification.where(isPublished()).or(authoredBy(currentUserName));
		if (authorName.isPresent()) {
			spec = spec.and(authoredBy(authorName.get()));
		}
		if (quizTitle.isPresent()) {
			spec = spec.and(titleContains(quizTitle.get()));
		}
		return orderByTitleDesc(spec);
	}

	static Specification<Quiz> isPublished() {
		return (root, query, cb) -> cb.isTrue(root.get(Quiz_.isPublished));
	}

	static Specification<Quiz> authoredBy(String authorName) {
		return (root, query, cb) -> cb.like(cb.upper(root.get(Quiz_.authorName)), "%%%s%%".formatted(authorName.toUpperCase()));
	}

	static Specification<Quiz> titleContains(String title) {
		return (root, query, cb) -> cb.like(cb.upper(root.get(Quiz_.title)), "%%%s%%".formatted(title.toUpperCase()));
	}

	static Specification<Quiz> orderByTitleDesc(Specification<Quiz> spec) {
		return (root, query, cb) -> {
			query.orderBy(cb.asc(root.get(Quiz_.title)));
			return spec.toPredicate(root, query, cb);
		};
	}
}
