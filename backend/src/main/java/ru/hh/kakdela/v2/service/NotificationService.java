package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.kakdela.v2.dao.PermissionDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dao.SurveyNotificationSubscriptionDao;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Survey;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SurveyDao surveyDao;
    private final PermissionDao permissionDao;
    private final SurveyNotificationSubscriptionDao subscriberDao;
   // private final EmailService emailService;

    @Async
    @Transactional(readOnly = true)
    public void sendSurveyPublishedNotifications(UUID surveyId) {
        Survey survey = surveyDao.findById(surveyId).orElse(null);
        if (survey == null || !survey.isPublished()) {
            log.warn("Survey {} is not published or not found", surveyId);
            return;
        }

        List<Account> teamMembers = getTeamMembers(surveyId);
        if (!teamMembers.isEmpty()) {
            sendTeamNotifications(survey, teamMembers);
        }

        List<Account> subscribers = getSubscribers(surveyId);
        if (!subscribers.isEmpty()) {
            sendSubscriberNotifications(survey, subscribers);
        }

        if (teamMembers.isEmpty() && subscribers.isEmpty()) {
            log.info("No recipients for survey {}", surveyId);
        }
    }

    private List<Account> getTeamMembers(UUID surveyId) {
        List<Permission> permissions = permissionDao.findAllBySurveyId(surveyId);
        return permissions.stream()
            .filter(Permission::isDoNotify)
            .map(Permission::getAccount)
            .collect(Collectors.toList());
    }

    private List<Account> getSubscribers(UUID surveyId) {
        return subscriberDao.findSubscribersBySurveyId(surveyId);
    }

    private void sendTeamNotifications(Survey survey, List<Account> teamMembers) {
        log.info("Sending {} team notifications for survey {}", teamMembers.size(), survey.getId());

        for (Account account : teamMembers) {
            String email = account.getEmail();
            log.info("Survey is published: {} - {}", survey.getTitle(), email);
            // emailService.sendSurveyPublishedEmail(email, survey.getTitle(), survey.getId());
        }
    }

    private void sendSubscriberNotifications(Survey survey, List<Account> subscribers) {
        log.info("Sending {} subscriber notifications for survey {}", subscribers.size(), survey.getId());

        for (Account account : subscribers) {
            String email = account.getEmail();
            log.info("You are invited to take survey: {} - {}", survey.getTitle(), email);
            // emailService.sendSurveyInvitationEmail(email, survey.getTitle(), survey.getId());
        }
    }
}