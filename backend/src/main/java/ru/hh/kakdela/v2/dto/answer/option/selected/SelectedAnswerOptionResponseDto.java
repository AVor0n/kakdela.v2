package ru.hh.kakdela.v2.dto.answer.option.selected;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(
    name = "SelectedAnswerOption.Response"
)
public class SelectedAnswerOptionResponseDto {

  private final UUID id;
  private final String answerOptionTextSnapshot;
}
