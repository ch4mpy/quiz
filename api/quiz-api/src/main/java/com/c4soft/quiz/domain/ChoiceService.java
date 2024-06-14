package com.c4soft.quiz.domain;

import org.springframework.stereotype.Service;
import com.c4soft.quiz.domain.jpa.ChoiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChoiceService {
  private final ChoiceRepository choiceRepo;

  public void deleteAll(Iterable<Choice> choices) {
    if (choices == null) {
      return;
    }
    this.choiceRepo.deleteAll(choices);
  }

}
