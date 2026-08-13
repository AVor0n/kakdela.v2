package ru.hh.kakdela.v2.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.bookmark.TemplateBookmarkResponseDto;
import ru.hh.kakdela.v2.model.TemplateBookmark;

@Component
@RequiredArgsConstructor
public class TemplateBookmarkMapper {

  public TemplateBookmarkResponseDto toTemplateBookmarkDto(TemplateBookmark bookmark) {
    return TemplateBookmarkResponseDto.builder()
        .id(bookmark.getId())
        .title(bookmark.getTemplate().getTitle())
        .createdAt(bookmark.getCreatedAt())
        .build();
  }
}
