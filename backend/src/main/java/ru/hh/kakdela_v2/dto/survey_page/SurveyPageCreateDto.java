package ru.hh.kakdela_v2.dto.survey_page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyPageCreateDto {

  @NotNull(message = "Порядковый номер обязателен")
  @Min(value = 1, message = "Порядковый номер должен быть больше 0")
  private Integer serialNumber;
  @NotBlank(message = "Заголовок не может быть пустым")
  @Size(max = 150, message = "Заголовок не может быть длиннее 150 символов")
  private String title;
  @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
  private String description;
}
