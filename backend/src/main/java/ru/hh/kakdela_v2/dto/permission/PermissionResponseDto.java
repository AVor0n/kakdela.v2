package ru.hh.kakdela_v2.dto.permission;

import lombok.Getter;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;

import java.util.UUID;

@Getter
public class PermissionResponseDto {

    private final UUID accountId;
    private final UUID surveyId;
    private final SurveyRole role;
    private final boolean doNotify;

    public PermissionResponseDto(Permission permission) {
        this.accountId = permission.getAccount().getId();
        this.surveyId = permission.getSurvey().getId();
        this.role = permission.getRole();
        this.doNotify = permission.isDoNotify();
    }
}
