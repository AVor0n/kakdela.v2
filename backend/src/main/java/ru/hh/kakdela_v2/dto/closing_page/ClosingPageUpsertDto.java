package ru.hh.kakdela_v2.dto.closing_page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ClosingPageUpsertDto {

  private String title;
  private String description;
  private String websiteUrl;
}
