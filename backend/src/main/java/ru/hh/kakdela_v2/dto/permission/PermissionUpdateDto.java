package ru.hh.kakdela_v2.dto.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermissionUpdateDto {

  private String role;        // nullable — если не передан, не меняем
  private Boolean doNotify;   // nullable — если не передан, не меняем
}
