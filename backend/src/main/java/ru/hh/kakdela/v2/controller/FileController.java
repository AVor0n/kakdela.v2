package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hh.kakdela.v2.dto.file.FileDownloadDto;
import ru.hh.kakdela.v2.dto.file.FileUploadResponseDto;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.ClosingPageService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Управление файлами завершающей страницы")

public class FileController {

  private final ClosingPageService closingPageService;

  @PostMapping("/surveys/{surveyId}/closing-page/file")
  @ResponseStatus(HttpStatus.CREATED)
  public FileUploadResponseDto uploadFile(
      @PathVariable UUID surveyId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.addFile(surveyId, currentUser.getId(), file);
  }

  @PutMapping("/surveys/{surveyId}/closing-page/file")
  public FileUploadResponseDto updateFile(
      @PathVariable UUID surveyId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.updateFile(surveyId, currentUser.getId(), file);
  }

  @DeleteMapping("/surveys/{surveyId}/closing-page/file")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFile(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    closingPageService.deleteFile(surveyId, currentUser.getId());
  }

  @GetMapping("/surveys/{surveyId}/closing-page/file/url")
  public ObjectUrlResponseDto getFileUrl(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.getFileUrl(
        surveyId,
        currentUser != null ? currentUser.getId() : null);
  }

  @GetMapping("/surveys/{surveyId}/closing-page/file/download")
  public FileDownloadDto downloadFile(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return closingPageService.getFileForDownload(
        surveyId,
        currentUser != null ? currentUser.getId() : null
    );
  }
}
