package ru.hh.kakdela.v2.mapper;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.template.TemplateResponseDto;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Component
@RequiredArgsConstructor
public class TemplateMapper {

  private final SurveyPageMapper surveyPageMapper;
  private final ClosingPageMapper closingPageMapper;

  public TemplateResponseDto templateToDto(Survey template) {
    return TemplateResponseDto.builder()
        .id(template.getId())
        .authorId(template.getAuthor().getId())
        .authorName(template.getAuthor().getLogin())
        .title(template.getTitle())
        .description(template.getDescription())
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
