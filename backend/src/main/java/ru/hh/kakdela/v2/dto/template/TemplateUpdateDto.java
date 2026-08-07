package ru.hh.kakdela.v2.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Template.Update")
public class TemplateUpdateDto {
  @Size(max = 200, message = "Название не должно быть длиннее 200 символов")
  private String title;
  private String description;
  private Boolean isPublished;
}
