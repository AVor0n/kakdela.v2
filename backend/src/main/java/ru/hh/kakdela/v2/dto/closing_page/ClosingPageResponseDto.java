package ru.hh.kakdela.v2.dto.closing_page;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClosingPageResponseDto {

  private final String title;
  private final String description;
  private final String attachmentUrl;
  private final String websiteUrl;
}
