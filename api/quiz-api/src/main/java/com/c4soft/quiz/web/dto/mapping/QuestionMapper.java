package com.c4soft.quiz.web.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.c4soft.quiz.domain.Question;
import com.c4soft.quiz.web.dto.QuestionDto;
import com.c4soft.quiz.web.dto.QuestionUpdateDto;

@Mapper(componentModel = "spring", uses = {ChoiceMapper.class})
public interface QuestionMapper {

  @Mapping(target = "questionId", source = "id")
  @Mapping(target = "quizId", source = "quiz.id")
  QuestionDto toDto(Question entity);

  @Mapping(target = "add", ignore = true)
  @Mapping(target = "remove", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "priority", ignore = true)
  @Mapping(target = "quiz", ignore = true)
  @Mapping(target = "choices", ignore = true)
  Question update(QuestionUpdateDto dto, @MappingTarget Question entity);
}
