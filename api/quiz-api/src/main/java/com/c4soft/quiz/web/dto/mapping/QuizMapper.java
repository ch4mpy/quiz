package com.c4soft.quiz.web.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.c4soft.quiz.domain.Quiz;
import com.c4soft.quiz.web.dto.QuizDto;
import com.c4soft.quiz.web.dto.QuizUpdateDto;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface QuizMapper {

  @Mapping(target = "isReplaced", expression = "java(entity.getReplacedBy() != null)")
  @Mapping(target = "draftId", source = "draft.id")
  @Mapping(target = "replacesId", source = "replaces.id")
  QuizDto toDto(Quiz entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "add", ignore = true)
  @Mapping(target = "remove", ignore = true)
  @Mapping(target = "authorName", ignore = true)
  @Mapping(target = "draft", ignore = true)
  @Mapping(target = "isPublished", ignore = true)
  @Mapping(target = "isSubmitted", ignore = true)
  @Mapping(target = "moderatedBy", ignore = true)
  @Mapping(target = "moderatorComment", ignore = true)
  @Mapping(target = "replacedBy", ignore = true)
  @Mapping(target = "replaces", ignore = true)
  @Mapping(target = "questions", ignore = true)
  @Mapping(target = "skillTests", ignore = true)
  Quiz update(QuizUpdateDto dto, @MappingTarget Quiz entity);
}
