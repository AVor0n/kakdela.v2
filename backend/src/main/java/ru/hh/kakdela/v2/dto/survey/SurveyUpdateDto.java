package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import ru.hh.kakdela.v2.validator.JsonNullableSize;
import ru.hh.kakdela.v2.validator.JsonNullableUndefinedOrNotNullAndNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Survey.Update")
public class SurveyUpdateDto {

  @JsonNullableUndefinedOrNotNullAndNotBlank(message = "Заголовок не должен быть пустым")
  @JsonNullableSize(max = 200, message = "Заголовок не должен быть длиннее 200 символов")
  private JsonNullable<String> title = JsonNullable.undefined();
  private JsonNullable<String> description = JsonNullable.undefined();
  private JsonNullable<Boolean> isAuthorizedOnly = JsonNullable.undefined();
  private JsonNullable<Boolean> isLimitedToOneResponse = JsonNullable.undefined();
  private JsonNullable<Boolean> isPublished = JsonNullable.undefined();
  private JsonNullable<Boolean> doNotify = JsonNullable.undefined();
  private JsonNullable<LocalDateTime> expireAtAtTargetTimezone = JsonNullable.undefined();
  @JsonNullableUndefinedOrNotNullAndNotBlank(message = "Часовой пояс не может быть пустым")
  private JsonNullable<String> targetTimezone = JsonNullable.undefined();
}
