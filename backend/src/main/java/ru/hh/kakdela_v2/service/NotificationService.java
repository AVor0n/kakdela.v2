package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.PermissionDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyNotificationSubscriberDao;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Survey;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SurveyDao surveyDao;
    private final AccountDao accountDao;
    private final PermissionDao permissionDao;
    private final SurveyNotificationSubscriberDao subscriberDao;
    //private final EmailService emailService;

    @Async
    @Transactional(readOnly = true)
    public void sendSurveyPublishedNotifications(UUID surveyId) {
        Survey survey = surveyDao.findById(surveyId).orElse(null);
        if (survey == null || !survey.isPublished()) {
            log.warn("Survey {} is not published or not found", surveyId);
            return;
        }

        List<UUID> recipients = getRecipients(surveyId);

        if (recipients.isEmpty()) {
            log.info("No recipients for survey {}", surveyId);
            return;
        }

        log.info("Sending {} notifications for survey {}", recipients.size(), surveyId);

        for (UUID userId : recipients) {
            Account account = accountDao.findById(userId).orElse(null);
            if (account != null) {
                String email = account.getEmail();
                if (email != null && !email.isBlank()) {
                    /*try {
                        emailService.sendSurveyPublishedEmail(email, survey.getTitle(), surveyId);
                        sentCount++;
                    } catch (Exception e) {
                        log.error("Failed to send email to {}: {}", email, e.getMessage());
                    }*/
                   log.info(" Would send email to: {} (survey: {})", email, surveyId);
                }
            }
        }
    }

    private List<UUID> getRecipients(UUID surveyId) {
        Set<UUID> recipients = new HashSet<>();
        Survey survey = surveyDao.findById(surveyId).orElse(null);
        if (survey == null) {
            return new ArrayList<>();
        }

        List<Permission> permissions = permissionDao.findAllBySurveyId(surveyId);
        for (Permission permission : permissions) {
            if (permission.isDoNotify()) {
                recipients.add(permission.getId().getAccountId());
                log.debug("Added user {} with role {} to recipients", 
                    permission.getId().getAccountId(), permission.getRole());
            }
        }

        List<UUID> subscribers = subscriberDao.findSubscriberIdsBySurveyId(surveyId);
        if (subscribers != null && !subscribers.isEmpty()) {
            recipients.addAll(subscribers);
            log.debug("Added {} subscribers to recipients", subscribers.size());
        }

        recipients.remove(null);
        return new ArrayList<>(recipients);
    }
}
