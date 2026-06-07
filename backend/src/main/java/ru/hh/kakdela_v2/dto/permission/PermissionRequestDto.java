package ru.hh.kakdela_v2.dto.permission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDto {

    @NotNull(message = "ID пользователя обязателен")
    private UUID accountId;

    @NotNull(message = "Роль обязательна")
    private SurveyRole role;

    private boolean doNotify;
}