package ru.hh.kakdela.v2.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hh.kakdela.v2.constants.Messages;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Template.Update")
public class TemplateUpdateDto {

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @NullOrNotBlank(message = Messages.FIELD_SHOULD_NOT_BE_EMPTY)
  private String title;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String description;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean isPublished;
}
