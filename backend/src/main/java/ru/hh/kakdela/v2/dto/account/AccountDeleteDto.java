package ru.hh.kakdela.v2.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "AccountDelete",
    title = "DTO для удаления аккаунта"
)
public class AccountDeleteDto {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Операция должна быть подтверждена вводом текущего пароля")
  private String password;
}
