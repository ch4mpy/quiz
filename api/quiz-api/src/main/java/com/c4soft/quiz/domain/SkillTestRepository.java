package com.c4soft.quiz.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillTestRepository extends JpaRepository<SkillTest, Long> {
    Optional<SkillTest> findByIdQuizIdAndIdTraineeName(Long quizId, String traineeName);
    List<SkillTest> findByIdQuizId(Long quizId);
}
