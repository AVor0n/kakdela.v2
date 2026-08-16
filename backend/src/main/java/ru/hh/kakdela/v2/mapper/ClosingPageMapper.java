package ru.hh.kakdela.v2.mapper;

import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.closing.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.file.FileResponseDto;
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

    FileResponseDto fileInfo = null;
    if (closingPage.getFileObjectKey() != null) {
      try {
        String fileKey = closingPage.getFileObjectKey();
        String fileName = Paths.get(fileKey).getFileName().toString();
        Long fileSize = objectStorageService.getFileSize(fileKey);

        fileInfo = FileResponseDto.builder()
            .fileName(fileName)
            .fileSize(fileSize)
            .build();
      } catch (Exception e) {
        fileInfo = null;
      }
    }

    return new ClosingPageResponseDto(
        closingPage.getTitle(),
        closingPage.getDescription(),
        attachmentUrl,
        closingPage.getWebsiteUrl(),
        fileInfo);
  }
}
