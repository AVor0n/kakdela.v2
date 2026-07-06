package ru.hh.kakdela.v2.dto.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.answer_option.AnswerOptionResponseDto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@Getter
@Schema(
    name = "QuestionResponse",
    title = "DTO для получения данных вопроса"
)
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
