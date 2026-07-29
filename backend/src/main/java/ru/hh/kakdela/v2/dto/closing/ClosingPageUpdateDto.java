package ru.hh.kakdela.v2.dto.closing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "ClosingPageUpdate",
    title = "DTO для обновления завершающей страницы"
)
public class ClosingPageUpdateDto {

  @NullOrNotBlank(message = "Заголовок не должен быть пустым")
  @Size(max = 200, message = "Заголовок завершающей страницы не должен быть длиннее 200 символов")
  private String title;

  @Size(max = 5000, message = "Текст завершающей страницы не должен быть длиннее 5000 символов")
  private String description;

  @NullOrNotBlank(message = "Ссылка на сайт не должна быть пустой")
  @Size(max = 2000, message = "Ссылка на сайт не должна быть длиннее 2000 символов")
  private String websiteUrl;
}
