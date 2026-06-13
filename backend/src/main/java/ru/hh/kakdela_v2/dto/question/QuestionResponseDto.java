package ru.hh.kakdela_v2.dto.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.Question;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class QuestionResponseDto {

  private final UUID id;
  private final int serialNumber;
  private final String title;
  private final String description;
  private final String type;
  private final String answerOptionOrder;
  private final boolean isMandatory;
  private final boolean isVisible;
  private final String condition;
  private final List<AnswerOptionResponseDto> answerOptions;

  public QuestionResponseDto(Question question) {
    this.id = question.getId();
    this.serialNumber = question.getSerialNumber();
    this.title = question.getTitle();
    this.description = question.getDescription();
    this.type = question.getType().name();
    this.answerOptionOrder = question.getAnswerOptionOrder() != null
        ? question.getAnswerOptionOrder().name()
        : null;
    this.isMandatory = question.isMandatory();
    this.isVisible = question.isVisible();
    this.condition = question.getCondition();
    this.answerOptions = question.getAnswerOptions().stream()
        .sorted(Comparator.comparingInt(a -> a.getSerialNumber()))
        .map(AnswerOptionResponseDto::new)
        .toList();
  }
}
