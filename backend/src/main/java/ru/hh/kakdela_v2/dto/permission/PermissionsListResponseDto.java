package ru.hh.kakdela_v2.dto.permission;

import lombok.Getter;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class PermissionsListResponseDto {

    private final UUID surveyId;
    private final List<PermissionItem> permissions;

    public PermissionsListResponseDto(UUID surveyId, List<Permission> permissions) {
        this.surveyId = surveyId;
        this.permissions = permissions.stream()
                .map(PermissionItem::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class PermissionItem {
        private final UUID accountId;
        private final SurveyRole role;
        private final boolean doNotify;

        public PermissionItem(Permission permission) {
            this.accountId = permission.getAccount().getId();
            this.role = permission.getRole();
            this.doNotify = permission.isDoNotify();
        }
    }
}