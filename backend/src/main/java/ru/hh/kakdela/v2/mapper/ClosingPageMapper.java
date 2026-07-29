package ru.hh.kakdela.v2.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.survey.page.closing.ClosingPageResponseDto;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.service.ObjectStorageService;

@Component
@RequiredArgsConstructor
public class ClosingPageMapper {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final ObjectStorageService objectStorageService;

  public ClosingPageResponseDto closingPageToDto(ClosingPage closingPage) {
    String attachmentUrl = closingPage.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
            closingPage.getAttachmentObjectKey(),
            attachmentUrlMaxAge).toString()
        : null;

    return new ClosingPageResponseDto(
        closingPage.getTitle(),
        closingPage.getDescription(),
        attachmentUrl,
        closingPage.getWebsiteUrl());
  }
}
