package ru.hh.kakdela_v2.dto.closing_page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.ClosingPage;

@AllArgsConstructor
@Getter
public class ClosingPageResponseDto {

  @NotBlank(message = "Заголовок завершающей страницы не должен быть пустым")
  @Max(value = 200, message = "Заголовок завершающей страницы не должен быть длиннее 200 символов")
  private final String title;
  @Max(value = 5000, message = "Текст завершающей страницы не должен быть длиннее 5000 символов")
  private final String description;
  @Max(value = 5000, message = "Ссылка на сайт не должна быть длиннее 5000 символов")
  private final String websiteUrl;

  public ClosingPageResponseDto(ClosingPage closingPage) {
    this.title = closingPage.getTitle();
    this.description = closingPage.getDescription();
    this.websiteUrl = closingPage.getWebsiteUrl();
  }
}
