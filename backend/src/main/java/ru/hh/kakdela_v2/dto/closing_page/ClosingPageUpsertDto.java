package ru.hh.kakdela_v2.dto.closing_page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClosingPageUpsertDto {

  private String title;
  private String description;
  private String websiteUrl;
}
