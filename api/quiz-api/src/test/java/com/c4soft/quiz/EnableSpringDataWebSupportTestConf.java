/* (C)2024 */
package com.c4soft.quiz;

import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.domain.jpa.ChoiceRepository;
import com.c4soft.quiz.domain.jpa.QuestionRepository;
import com.c4soft.quiz.domain.jpa.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Avoid MethodArgumentConversionNotSupportedException with mocked repos
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@TestConfiguration
@AutoConfigureDataJpa
public class EnableSpringDataWebSupportTestConf {
    @Autowired QuizRepository quizRepo;

    @Autowired QuestionRepository questionRepo;

    @Autowired ChoiceRepository choiceRepo;

    @Bean
    WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(
                        Long.class, Quiz.class, id -> quizRepo.findById(id).orElse(null));
                registry.addConverter(
                        Long.class, Question.class, id -> questionRepo.findById(id).orElse(null));
                registry.addConverter(
                        Long.class, Choice.class, id -> choiceRepo.findById(id).orElse(null));
            }
        };
    }
}
