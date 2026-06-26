package ru.hh.kakdela.v2.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.hh.kakdela.v2.dto.object.ObjectUrlResponseDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AnswerOptionService;
import ru.hh.kakdela.v2.service.QuestionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

  private final QuestionService questionService;
  private final AnswerOptionService answerOptionService;

  // Question attachment

  @PostMapping(
      value = "/questions/{questionId}/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ObjectUrlResponseDto addAttachmentToQuestion(
      @PathVariable UUID questionId,
      @RequestParam MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return questionService.addAttachment(
        questionId,
        currentUser.getId(),
        file
    );
  }

  @PutMapping(
      value = "/questions/{questionId}/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ObjectUrlResponseDto updateAttachmentInQuestion(
      @PathVariable UUID questionId,
      @RequestParam MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return questionService.updateAttachment(
        questionId,
        currentUser.getId(),
        file
    );
  }

  @DeleteMapping("/questions/{questionId}/attachment")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAttachmentFromQuestion(
      @PathVariable UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    questionService.deleteAttachment(
        questionId,
        currentUser.getId()
    );
  }

  // AnswerOption attachment

  @PostMapping(
      value = "/answer-options/{answerOptionId}/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ObjectUrlResponseDto addAttachmentToAnswerOption(
      @PathVariable UUID answerOptionId,
      @RequestParam MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return answerOptionService.addAttachment(
        answerOptionId,
        currentUser.getId(),
        file
    );
  }

  @PutMapping(
      value = "/answer-options/{answerOptionId}/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ObjectUrlResponseDto updateAttachmentInAnswerOption(
      @PathVariable UUID answerOptionId,
      @RequestParam MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return answerOptionService.updateAttachment(
        answerOptionId,
        currentUser.getId(),
        file
    );
  }

  @DeleteMapping("/answer-options/{answerOptionId}/attachment")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAttachmentFromAnswerOption(
      @PathVariable UUID answerOptionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    answerOptionService.deleteAttachment(
        answerOptionId,
        currentUser.getId()
    );
  }
}
