package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "SurveyUpdate",
    title = "DTO для обновления опроса"
)
public class SurveyUpdateDto {

  @NullOrNotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private String title;
  private String description;
  private Boolean isAuthorizedOnly;
  private Boolean isLimitedToOneResponse;
  private Boolean isPublished;
  private Boolean doNotify;
  @FutureOrPresent(message = "Дедлайн не должен быть в прошлом")
  private LocalDateTime expireAtAtTargetTimezone;
  @NullOrNotBlank(message = "Часовой пояс не может быть пустым")
  private String targetTimezone;
}
