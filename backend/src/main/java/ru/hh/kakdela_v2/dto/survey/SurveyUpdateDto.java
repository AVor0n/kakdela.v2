package ru.hh.kakdela_v2.dto.survey;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class SurveyUpdateDto {

  @Size(min = 1, max = 200, message = "Заголовок не может быть пустым или длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
  private String description;
  private Boolean authorizedOnly;
  private Boolean limitedToOneResponse;
  private Boolean published;
  private Boolean doNotify;
  @FutureOrPresent(message = "Дедлайн не может быть в прошлом")
  private Instant expireAt;
}
