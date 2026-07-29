package ru.hh.kakdela.v2.dto.survey.page;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "SurveyPage.Create"
)
public class SurveyPageCreateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
}
