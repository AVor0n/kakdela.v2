package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "PasswordReset.Request"
)
public class PasswordResetDto {
  @NullOrNotBlank(message = "Электронная почта не должна быть пустой")
  String email;
  @NullOrNotBlank(message = "Код не должен быть пустым")
  String code;
  @NullOrNotBlank(message = "Пароль не должен быть пустым")
  String newPassword;
  @NullOrNotBlank(message = "Подтверждение пароля не должно быть пустым")
  String passwordConfirmation;
}
