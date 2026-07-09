package ru.hh.kakdela.v2.dto.survey_subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(
    name = "SurveySubscriptionResponse",
    title = "DTO для получения данных о подписчиках опроса для уведомлений"
)
public class SurveySubscriptionResponseDto {

  private List<String> subscribedEmails;
  private List<String> alreadySubscribedEmails;
  private List<String> notFoundEmails;

}
