package com.c4soft.quiz.web.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.c4soft.quiz.domain.Choice;
import com.c4soft.quiz.web.dto.ChoiceDto;
import com.c4soft.quiz.web.dto.ChoiceUpdateDto;

@Mapper(componentModel = "spring")
public interface ChoiceMapper {

  @Mapping(target = "choiceId", source = "id")
  @Mapping(target = "questionId", source = "question.id")
  @Mapping(target = "quizId", source = "question.quiz.id")
  ChoiceDto toDto(Choice entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  Choice update(ChoiceUpdateDto dto, @MappingTarget Choice entity);
}
