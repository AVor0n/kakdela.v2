package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.SurveyNotificationSubscriberDao;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyNotificationSubscriberService {

    private final SurveyNotificationSubscriberDao subscriberDao;
    private final AccountDao accountDao;
    private final PermissionService permissionService;

    @Transactional
    public void subscribeUsers(UUID surveyId, List<UUID> accountIds, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

        if (accountIds == null || accountIds.isEmpty()) {
            log.info("No users to subscribe for survey {}", surveyId);
            return;
        }

        for (UUID accountId : accountIds) {
            accountDao.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Аккаунт не найден: " + accountId));

            subscriberDao.subscribe(surveyId, accountId);
            log.info("User {} subscribed to notifications for survey {}", accountId, surveyId);
        }
    }

    @Transactional
    public void unsubscribeUser(UUID surveyId, UUID accountId, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

        subscriberDao.unsubscribe(surveyId, accountId);
        log.info("User {} unsubscribed from notifications for survey {}", accountId, surveyId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getSubscribers(UUID surveyId, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);
        return subscriberDao.findSubscriberIdsBySurveyId(surveyId);
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(UUID surveyId, UUID accountId, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);
        return subscriberDao.existsBySurveyIdAndAccountId(surveyId, accountId);
    }
}
