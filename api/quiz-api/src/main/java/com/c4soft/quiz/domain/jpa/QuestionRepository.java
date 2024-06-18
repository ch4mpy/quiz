/* (C)2024 */
package com.c4soft.quiz.domain.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.quiz.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
