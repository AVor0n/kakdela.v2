package ru.hh.kakdela.v2.dto.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
    name = "TemplateBookmark.Response"
)
public class TemplateBookmarkResponseDto {

  private final UUID id;
  private final String title;
  private final Instant createdAt;
}
