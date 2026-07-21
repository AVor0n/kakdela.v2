package ru.hh.kakdela.v2.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.model.Permission;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "PermissionUpdate",
    title = "DTO для обновления роли"
)
public class PermissionUpdateDto {

  private UUID accountId;
  private Permission.SurveyRole role;
  private Boolean doNotify; // nullable — если не передан, не меняем
}
