package ru.hh.kakdela_v2.dto.closing_page;

import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ClosingPageUpsertDto {

  @Max(value = 200, message = "Заголовок завершающей страницы не должен быть длиннее 200 символов")
  private String title;
  @Max(value = 5000, message = "Текст завершающей страницы не должен быть длиннее 5000 символов")
  private String description;
  @Max(value = 5000, message = "Ссылка на сайт не должна быть длиннее 5000 символов")
  private String websiteUrl;
}
