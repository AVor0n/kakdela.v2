package ru.hh.kakdela.v2.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.answer.option.selected.SelectedAnswerOptionResponseDto;

@AllArgsConstructor
@Getter
@Schema(
    name = "Answer.Response"
)
public class AnswerResponseDto {

  private final UUID responseId;
  private final UUID questionId;
  private final String questionTextSnapshot;
  private final String textValue;
  private final Boolean booleanValue;
  private final LocalDate dateValue;
  private final LocalTime timeValue;
  private final List<SelectedAnswerOptionResponseDto> selectedAnswerOptions;
  private final String answerAsString;
}
