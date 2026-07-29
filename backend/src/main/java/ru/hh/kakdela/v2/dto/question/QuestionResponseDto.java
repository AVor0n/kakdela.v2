package ru.hh.kakdela.v2.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(
    name = "Question.Response"
)
public class QuestionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String text;
  private final String description;
  private final String attachmentUrl;
  private final String type;
  private final String answerOptionOrder;
  private final Boolean hasOtherOption;
  private final Boolean isMandatory;
  private final List<AnswerOptionResponseDto> answerOptions;
}
