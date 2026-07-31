package ru.hh.kakdela.v2.dto.survey.page.closing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "ClosingPageUpsert",
    title = "DTO для создания или обновления завершающей страницы"
)
public class ClosingPageUpsertDto {

  @Size(max = 200, message = "Заголовок завершающей страницы не должен быть длиннее 200 символов")
  private String title;
  private String description;
  @Size(max = 2000, message = "Ссылка на сайт не должна быть длиннее 2000 символов")
  private String websiteUrl;
}
