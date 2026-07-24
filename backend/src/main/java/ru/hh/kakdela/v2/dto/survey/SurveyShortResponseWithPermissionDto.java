package ru.hh.kakdela.v2.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.model.Permission;

@AllArgsConstructor
@Getter
@Schema(
    name = "SurveyShortResponseWithPermissionDto",
    title = "Краткий DTO данных опроса с указанием прав пользователя"
)
public class SurveyShortResponseWithPermissionDto {

  private final UUID id;
  private final String title;
  private final String description;
  private final Boolean isPublished;
  private final Instant createdAt;
  private final Permission.SurveyRole userRole;
}
