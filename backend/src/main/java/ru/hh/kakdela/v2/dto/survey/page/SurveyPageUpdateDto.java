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
    name = "SurveyPage.Update"
)
public class SurveyPageUpdateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;

  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;

  private String description;
}
