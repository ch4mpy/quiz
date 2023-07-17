package com.c4soft.quiz.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SkillTestRepository extends JpaRepository<SkillTest, Long>, JpaSpecificationExecutor<SkillTest> {
	Optional<SkillTest> findByIdQuizIdAndIdTraineeName(Long quizId, String traineeName);

	List<SkillTest> findByIdQuizId(Long quizId);

	static Specification<SkillTest> quizIdSpec(Long quizId) {
		return (skillTest, cq, cb) -> cb.equal(skillTest.get("id").get("quizId"), quizId);
	}

	static Specification<SkillTest> traineeNameSpec(String traineeName) {
		return (skillTest, cq, cb) -> cb.equal(skillTest.get("id").get("traineeName"), traineeName);
	}

	static Specification<SkillTest> sinceSpec(Long since) {
		return (skillTest, cq, cb) -> cb.ge(skillTest.get("submittedOn"), since);
	}

	static Specification<SkillTest> untilSpec(Long until) {
		return (skillTest, cq, cb) -> cb.le(skillTest.get("submittedOn"), until);
	}

	static Specification<SkillTest> spec(Long quizId, Long since, Optional<Long> until, Optional<String> traineeName) {
		final var spec = Specification.where(quizIdSpec(quizId));
		spec.and(sinceSpec(since));
		spec.and(untilSpec(until.orElse(Instant.now().toEpochMilli())));
		traineeName.ifPresent(n -> spec.and(traineeNameSpec(n)));
		return spec;
	}
}
