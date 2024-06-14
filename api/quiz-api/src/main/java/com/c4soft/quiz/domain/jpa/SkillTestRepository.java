/* (C)2024 */
package com.c4soft.quiz.domain.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.c4soft.quiz.domain.SkillTest;
import com.c4soft.quiz.domain.SkillTest.SkillTestPk;
import com.c4soft.quiz.domain.SkillTest_;

public interface SkillTestRepository
    extends JpaRepository<SkillTest, SkillTestPk>, JpaSpecificationExecutor<SkillTest> {
  Optional<SkillTest> findByIdQuizIdAndIdTraineeName(Long quizId, String traineeName);

  List<SkillTest> findByIdQuizId(Long quizId);

  void deleteByIdQuizId(Long quizId);

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

  static Specification<SkillTest> orderByIdDesc(Specification<SkillTest> spec) {
    return (root, query, cb) -> {
      query.orderBy(cb.desc(root.get(SkillTest_.id)));
      return spec.toPredicate(root, query, cb);
    };
  }

  static Specification<SkillTest> spec(Long quizId, Long since, Optional<Long> until) {
    return orderByIdDesc(Specification.where(quizIdSpec(quizId)).and(sinceSpec(since))
        .and(untilSpec(until.orElse(Instant.now().toEpochMilli()))));
  }
}
