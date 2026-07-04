package ru.hh.kakdela.v2.dto.subscription;

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
    name = "SubscriptionResponse",
    title = "DTO для получения данных о подписчиках опроса для уведомлений"
)
public class SubscriptionResponseDto {

  private List<String> subscribedEmails;
  private List<String> alreadySubscribedEmails;
  private List<String> notFoundEmails;

}
