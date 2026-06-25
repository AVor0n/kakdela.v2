package ru.hh.kakdela_v2.dto.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.model.Question;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class QuestionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String title;
  private final String description;
  private final String attachmentUrl;
  private final String type;
  private final String answerOptionOrder;
  private final Boolean isMandatory;
  private final Boolean isVisible;
  private final String condition;
  private final List<AnswerOptionResponseDto> answerOptions;
}
