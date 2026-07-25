package ru.hh.kakdela.v2.mapper;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.service.ObjectStorageService;

@Component
@RequiredArgsConstructor
public class QuestionMapper {

  @Value("${app.attachments.url-max-age}")
  private long attachmentUrlMaxAge;

  private final ObjectStorageService objectStorageService;
  private final AnswerOptionMapper answerOptionMapper;

  public QuestionResponseDto questionToDto(Question question) {
    String attachmentUrl = question.getAttachmentObjectKey() != null
        ? objectStorageService.generateObjectUrl(
        question.getAttachmentObjectKey(),
        attachmentUrlMaxAge
    ).toString()
        : null;

    return new QuestionResponseDto(
        question.getId(),
        question.getSerialNumber(),
        question.getText(),
        question.getDescription(),
        attachmentUrl,
        question.getType().name(),
        question.getAnswerOptionOrder() != null
            ? question.getAnswerOptionOrder().name()
            : null,
        question.isMandatory(),
        question.isVisible(),
        question.getCondition(),
        question.getAnswerOptions().stream()
            .sorted(Comparator.comparingInt(AnswerOption::getSerialNumber))
            .map(answerOptionMapper::answerOptionToDto)
            .toList()
    );
  }
}
