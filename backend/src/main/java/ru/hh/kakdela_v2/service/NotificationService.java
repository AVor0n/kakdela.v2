package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.PermissionDao;
import ru.hh.kakdela_v2.dao.SurveyDao;
import ru.hh.kakdela_v2.dao.SurveyNotificationSettingsDao;
import ru.hh.kakdela_v2.model.Permission;
import ru.hh.kakdela_v2.model.Survey;
import ru.hh.kakdela_v2.model.SurveyNotificationSettings;



@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SurveyDao surveyDao;
    private final AccountDao accountDao;
    private final PermissionDao permissionDao;
    private final SurveyNotificationSettingsDao settingsDao;
    private final EmailService emailService;

    @Async
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

        int sentCount = 0;
        for (UUID userId : recipients) {
            String email = accountDao.findEmailById(userId);
            if (email != null && !email.isBlank()) {
                try {
                        emailService.sendSurveyPublishedEmail(email, survey.getTitle(), surveyId);
                        sentCount++;
                } catch (Exception e) {
                        log.error("Failed to send email to {}: {}", email, e.getMessage());
                    }
            }
        }

        log.info("Survey {} notifications sent to {} of {} recipients",
                surveyId, sentCount, recipients.size());
    }

    private List<UUID> getRecipients(UUID surveyId) {
        Set<UUID> recipients = new HashSet<>();
        Survey survey = surveyDao.findById(surveyId).orElse(null);
        if (survey == null) return new ArrayList<>();

        Optional<SurveyNotificationSettings> settingsOpt = settingsDao.findBySurveyId(surveyId);
        if (settingsOpt.isEmpty()) {
            return new ArrayList<>(recipients);
        }

        SurveyNotificationSettings settings = settingsOpt.get();

        if (settings.isNotifyEditors()) {
            List<UUID> editors = permissionDao.findUserIdsBySurveyIdAndRole(
                surveyId, Permission.SurveyRole.EDITOR.name()
            );
            if (editors != null && !editors.isEmpty()) {
                recipients.addAll(editors);
            }
        }

        if (settings.isNotifyAnalysts()) {
            List<UUID> analysts = permissionDao.findUserIdsBySurveyIdAndRole(
                surveyId, Permission.SurveyRole.ANALYST.name()
            );
            if (analysts != null && !analysts.isEmpty()) {
            recipients.addAll(analysts);
            }
        }

        if (settings.getNotifyCustomUserIds() != null) {
            recipients.addAll(settings.getNotifyCustomUserIds());
        }

        recipients.remove(null);
        return new ArrayList<>(recipients);
    }
}
