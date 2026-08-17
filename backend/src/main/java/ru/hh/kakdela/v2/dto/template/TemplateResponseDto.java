package ru.hh.kakdela.v2.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import ru.hh.kakdela.v2.dto.closing.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.survey.page.SurveyPageResponseDto;

@Data
@Builder
@Schema(
    name = "Template.Response"
)
public class TemplateResponseDto {
  private final UUID id;
  private final String title;
  private final String description;
  private String attachmentUrl;
  private final UUID authorId;
  private final String authorName;
  private final boolean isPublished;
  private final Instant createdAt;
  private final List<SurveyPageResponseDto> pages;
  private final ClosingPageResponseDto closingPage;
}
