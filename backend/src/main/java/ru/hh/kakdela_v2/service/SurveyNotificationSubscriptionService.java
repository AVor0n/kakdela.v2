package ru.hh.kakdela_v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.dao.SurveyNotificationSubscriptionDao;
import ru.hh.kakdela_v2.dto.subscription.SubscriptionResponseDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.model.Permission.SurveyRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyNotificationSubscriptionService {

    private final SurveyNotificationSubscriptionDao subscribtionDao;
    private final AccountDao accountDao;
    private final PermissionService permissionService;

    @Transactional
    public SubscriptionResponseDto subscribeUsers(UUID surveyId, List<String> emails, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

        if (emails == null || emails.isEmpty()) {
            return new SubscriptionResponseDto(List.of(), List.of(), List.of());
        }

        List<String> subscribedEmails = new ArrayList<>();
        List<String> alreadySubscribedEmails = new ArrayList<>();
        List<String> notFoundEmails = new ArrayList<>();

        for (String email : emails) {
            Account account = accountDao.findByEmail(email).orElse(null);
            if (account == null) {
                notFoundEmails.add(email);
                log.warn("User with email {} not found", email);
                continue;
            }

            UUID accountId = account.getId();

           
            try {
                subscribtionDao.addSubscription(surveyId, accountId);
                subscribedEmails.add(email);
                log.info("User {} subscribed to survey {}", email, surveyId);
            } catch (DataIntegrityViolationException e) {
                alreadySubscribedEmails.add(email);
                log.debug("User {} already subscribed", email);
            }
        }

        log.info("Subscribed {} users to survey {}", subscribedEmails.size(), surveyId);
        return new SubscriptionResponseDto(
            subscribedEmails,
            alreadySubscribedEmails,
            notFoundEmails
        );
    }

    @Transactional
    public void unsubscribeUser(UUID surveyId, String email, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

        Account account = findAccountByEmail(email);
        UUID accountId = account.getId();

        if (!subscribtionDao.existsBySurveyIdAndAccountId(surveyId, accountId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Подписка для " + email + " не найдена");
        }

        subscribtionDao.deleteSubscription(surveyId, accountId);
        log.info("User {} unsubscribed from survey {}", email, surveyId);
    }


    @Transactional(readOnly = true)
    public List<Account> getSubscribers(UUID surveyId, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);
        return subscribtionDao.findSubscribersBySurveyId(surveyId);
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(UUID surveyId, String email, UUID currentUserId) {
        permissionService.checkAccess(surveyId, currentUserId, SurveyRole.EDITOR);

        Account account = accountDao.findByEmail(email).orElse(null);
        if (account == null) {
            return false;
        }

        return subscribtionDao.existsBySurveyIdAndAccountId(surveyId, account.getId());
    }

    private Account findAccountByEmail(String email) {
        return accountDao.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Пользователь с email " + email + " не найден"));
    }
}