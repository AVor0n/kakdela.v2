package ru.hh.kakdela.v2.dto.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "SubscriptionRequest",
    title = "DTO для запроса на создание подписки на уведомления"
)
public class SubscriptionRequestDto {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Список почт получателей обязателен")
  private List<String> emails;
}
