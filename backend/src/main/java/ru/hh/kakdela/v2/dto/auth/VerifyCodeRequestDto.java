package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hh.kakdela.v2.validator.NullOrNotBlank;

@NoArgsConstructor
@Getter
@Setter
@Schema(
    name = "VerifyCode.Request"
)
public class VerifyCodeRequestDto {
  @NullOrNotBlank(message = "Электронная почта не должна быть пустой")
  String email;
  @NullOrNotBlank(message = "Код не должен быть пустым")
  String code;
}
