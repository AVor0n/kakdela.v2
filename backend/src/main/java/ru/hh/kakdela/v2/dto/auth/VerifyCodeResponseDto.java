package ru.hh.kakdela.v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(
    name = "VerifyCode.Response"
)
public class VerifyCodeResponseDto {
  private boolean success;
  private boolean blocked;
  private String message;
  private int remainingAttempts;
}
