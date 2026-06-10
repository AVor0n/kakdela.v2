package ru.hh.kakdela_v2.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.hh.kakdela_v2.dao.PermissionDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dto.permission.PermissionRequestDto;
import ru.hh.kakdela_v2.dto.permission.PermissionResponseDto;
import ru.hh.kakdela_v2.dto.permission.PermissionsListResponseDto;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Permission.PermissionId;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.util.TransactionHelper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionDao permissionDao;
    private final SurveyDao surveyDao;
    private final AccountDao accountDao;
    private final TransactionHelper transactionHelper;

    public void checkAccess(UUID surveyId, UUID accountId, SurveyRole requiredRole) {
        Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

        if (survey.getAuthor().getId().equals(accountId)) {
            return;
        }

        Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к опросу"));

        if (!hasEnoughRole(permission.getRole(), requiredRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Недостаточно прав. Требуется: " + requiredRole);
        }
    }

    public void checkOwnership(UUID surveyId, UUID accountId) {
        Survey survey = surveyDao.findById(surveyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

        if (!survey.getAuthor().getId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не являетесь автором опроса");
        }
    }

    private boolean hasEnoughRole(SurveyRole userRole, SurveyRole requiredRole) {
        if (requiredRole == SurveyRole.EDITOR) {
            return userRole == SurveyRole.EDITOR;
        }
        if (requiredRole == SurveyRole.ANALYST) {
            return userRole == SurveyRole.EDITOR || userRole == SurveyRole.ANALYST;
        }
        return false;
    }

    public PermissionsListResponseDto getPermissionsBySurveyId(UUID surveyId) {
        return transactionHelper.inTransaction(() -> {
            List<Permission> permissions = permissionDao.findAllBySurveyId(surveyId);
            return new PermissionsListResponseDto(surveyId, permissions);
        });
    }

    public List<PermissionResponseDto> getPermissionsByAccountId(UUID accountId) {
        return transactionHelper.inTransaction(() ->
            permissionDao.findAllByAccountId(accountId).stream()
                .map(PermissionResponseDto::new)
                .toList()
        );
    }

    public PermissionResponseDto grantPermission(UUID surveyId, UUID currentUserId, PermissionRequestDto request) {
        return transactionHelper.inTransaction(() -> {
            checkOwnership(surveyId, currentUserId);

            Survey survey = surveyDao.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Опрос не найден: " + surveyId));

            Account account = accountDao.findById(request.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт не найден: " + request.getAccountId()));

            if (permissionDao.existsBySurveyIdAndAccountId(surveyId, request.getAccountId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Права уже выданы этому пользователю");
            }

            PermissionId permissionId = PermissionId.builder()
                .accountId(request.getAccountId())
                .surveyId(surveyId)
                .build();

            Permission permission = Permission.builder()
                .id(permissionId)
                .account(account)
                .survey(survey)
                .role(request.getRole())
                .doNotify(request.isDoNotify())
                .build();

            permissionDao.save(permission);
            return new PermissionResponseDto(permission);
        });
    }

    public PermissionResponseDto updatePermission(UUID surveyId, UUID accountId, UUID currentUserId, PermissionRequestDto request) {
        return transactionHelper.inTransaction(() -> {
            checkOwnership(surveyId, currentUserId);

            Permission permission = permissionDao.findBySurveyIdAndAccountId(surveyId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Права не найдены"));

            permission.setRole(request.getRole());
            permission.setDoNotify(request.isDoNotify());

            permissionDao.update(permission);
            return new PermissionResponseDto(permission);
        });
    }

    public void revokePermission(UUID surveyId, UUID accountId, UUID currentUserId) {
        transactionHelper.inTransaction(() -> {
            checkOwnership(surveyId, currentUserId);

            permissionDao.deleteBySurveyIdAndAccountId(surveyId, accountId);
        });
    }

   public List<Survey> getAccessibleSurveys(UUID accountId) {
    return transactionHelper.inTransaction(() -> {
        List<Survey> authored = surveyDao.findAllByAuthorId(accountId);
        List<Survey> shared = permissionDao.findAllByAccountId(accountId).stream()
            .map(Permission::getSurvey)
            .toList();

        return Stream.concat(authored.stream(), shared.stream())
                .distinct()
                .collect(Collectors.toList());
    });
}
}