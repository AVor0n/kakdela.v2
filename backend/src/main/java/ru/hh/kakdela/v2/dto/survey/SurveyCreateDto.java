package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.constants.DefaultValues;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "SurveyCreate",
    title = "DTO для создания опроса"
)
public class SurveyCreateDto {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  private String description;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean isAuthorizedOnly = DefaultValues.IS_AUTHORIZED_ONLY_DEFAULT;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean isLimitedToOneResponse = DefaultValues.IS_LIMITED_TO_ONE_RESPONSE_DEFAULT;
  @Schema(description = "Это необязательное поле, у него есть дефолтное значение")
  @NotNull
  private Boolean doNotify = DefaultValues.DO_NOTIFY_DEFAULT;
  private LocalDateTime expireAtAtTargetTimezone;
  @NullOrNotBlank(message = "Часовой пояс не может быть пустым")
  private String targetTimezone = DefaultValues.TARGET_TIMEZONE_DEFAULT;
}
