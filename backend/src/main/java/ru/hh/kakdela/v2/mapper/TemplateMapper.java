package ru.hh.kakdela.v2.mapper;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.template.TemplateResponseDto;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;
import ru.hh.kakdela.v2.service.ObjectStorageService;

@Component
@RequiredArgsConstructor
public class TemplateMapper {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final SurveyPageMapper surveyPageMapper;
  private final ClosingPageMapper closingPageMapper;
  private final ObjectStorageService objectStorageService;

  public TemplateResponseDto templateToDto(Survey template) {
    String attachmentUrl = template.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        template.getAttachmentObjectKey(),
        attachmentUrlMaxAge).toString()
        : null;

    return TemplateResponseDto.builder()
        .id(template.getId())
        .authorId(template.getAuthor().getId())
        .authorName(template.getAuthor().getLogin())
        .title(template.getTitle())
        .description(template.getDescription())
        .attachmentUrl(attachmentUrl)
        .isPublished(template.isPublished())
        .createdAt(template.getCreatedAt())
        .pages(template.getPages().stream()
            .sorted(Comparator.comparingInt(SurveyPage::getSerialNumber))
            .map(surveyPageMapper::surveyPageToDto)
            .toList())
        .closingPage(template.getClosingPage() != null
            ? closingPageMapper.closingPageToDto(template.getClosingPage())
            : null)
        .build();
  }

  public List<TemplateResponseDto> templatesToDtoList(List<Survey> templates) {
    return templates.stream()
        .map(this::templateToDto)
        .toList();
  }
}
