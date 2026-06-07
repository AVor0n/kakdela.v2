package ru.hh.kakdela_v2.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.FutureOrPresent;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCreateDto {
  @NotBlank(message = "Заголовок не может быть пустым")
  @Size(max = 200, message = "Заголовок не может быть длиннее 200 символов")
  private String title;
  @Size(max = 5000, message = "Описание не может быть длиннее 5000 символов")
  private String description;
  private boolean isAuthorizedOnly;
  private boolean isLimitedToOneResponse;
  private boolean doNotify;
  @FutureOrPresent(message = "Дедлайн не может быть в прошлом")
  private Instant expireAt;
}
