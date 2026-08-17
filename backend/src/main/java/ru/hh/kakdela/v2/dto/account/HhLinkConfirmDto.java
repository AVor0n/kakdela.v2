package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Auth.HhLink.Confirm")
public class HhLinkConfirmDto {

  @NotBlank(message = "Токен привязки обязателен")
  private String hhLinkToken;

  // Обязателен, только если пользователь не авторизован в момент подтверждения -
  // проверяется в сервисе (AccountService.confirmLinkHhSso), не через @NotBlank,
  // т.к. для авторизованного пользователя пароль не нужен
  private String password;
}
