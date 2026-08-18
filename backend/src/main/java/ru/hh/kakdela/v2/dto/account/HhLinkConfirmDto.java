package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Auth.HhLink.Confirm")
public class HhLinkConfirmDto {

  // Обязателен, только если пользователь не авторизован в момент подтверждения -
  // проверяется в сервисе (AccountService.confirmLinkHhSso), не через @NotBlank,
  // т.к. для авторизованного пользователя пароль не нужен
  @NullOrNotBlank
  private String password;
}