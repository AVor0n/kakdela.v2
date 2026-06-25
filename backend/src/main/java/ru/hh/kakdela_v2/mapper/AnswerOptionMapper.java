package ru.hh.kakdela_v2.mapper;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.AnswerOption;
import ru.hh.kakdela_v2.service.ObjectStorageService;

@Component
@RequiredArgsConstructor
public class AnswerOptionMapper {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final ObjectStorageService objectStorageService;

  public AnswerOptionResponseDto answerOptionToDto(AnswerOption answerOption) {
    String attachmentUrl = answerOption.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        answerOption.getAttachmentObjectKey(),
        attachmentUrlMaxAge
    ).toString()
        : null;

    return new AnswerOptionResponseDto(
        answerOption.getId(),
        answerOption.getSerialNumber(),
        answerOption.getAnswerOptionText(),
        attachmentUrl
    );
  }
}
