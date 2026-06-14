package ru.hh.kakdela_v2.dto.survey;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class SurveyCreateDto {
  @NotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
  private String description;
  private Boolean isAuthorizedOnly;
  private Boolean isLimitedToOneResponse;
  private Boolean doNotify;
  @FutureOrPresent(message = "Дедлайн не должен быть в прошлом")
  private Instant expireAt;
}
