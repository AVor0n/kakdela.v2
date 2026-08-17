package ru.hh.kakdela.v2.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;

@AllArgsConstructor
@Getter
@Schema(name = "Permission.Response")
public class PermissionResponseDto {

  private final UUID surveyId;
  private final AccountResponseDto account;
  private final String role;
  private final Boolean doNotify;
}
