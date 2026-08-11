package ru.hh.kakdela.v2.dto.survey.page;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Schema(
    name = "SurveyPage.ShortResponse"
)
@Getter
public class SurveyPageShortResponseDto {

  private final UUID id;
  private final int serialNumber;
}
