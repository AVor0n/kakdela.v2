package ru.hh.kakdela_v2.dto.closing_page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.model.ClosingPage;

@AllArgsConstructor
@Getter
public class ClosingPageResponseDto {

  private final String title;
  private final String description;
  private final String websiteUrl;

  public ClosingPageResponseDto(ClosingPage closingPage) {
    this.title = closingPage.getTitle();
    this.description = closingPage.getDescription();
    this.websiteUrl = closingPage.getWebsiteUrl();
  }
}
