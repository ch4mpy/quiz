package com.c4soft.quiz.domain;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.c4soft.quiz.domain.jpa.QuestionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {
  private final QuestionRepository questionRepo;
  private final ChoiceService choiceService;

  public void deleteAll(Collection<Question> questions) {
    if (questions == null) {
      return;
    }
    final var choices =
        questions.stream().flatMap(q -> q.getChoices().stream()).collect(Collectors.toSet());
    this.choiceService.deleteAll(choices);
    this.questionRepo.deleteAll(questions);
  }

}
