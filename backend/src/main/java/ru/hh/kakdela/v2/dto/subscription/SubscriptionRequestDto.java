package ru.hh.kakdela.v2.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
