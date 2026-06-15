package ru.hh.kakdela_v2.dto.survey_page;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SurveyPageUpdateDto {

  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
}
