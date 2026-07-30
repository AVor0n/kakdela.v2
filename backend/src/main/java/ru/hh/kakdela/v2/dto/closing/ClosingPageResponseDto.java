package ru.hh.kakdela.v2.dto.closing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Schema(
    name = "ClosingPage.Response"
)
public class ClosingPageResponseDto {

  private final String title;
  private final String description;
  private final String attachmentUrl;
  private final String websiteUrl;
}
