package ru.hh.kakdela_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.subscription.SubscriptionRequestDto;
import ru.hh.kakdela_v2.dto.subscription.SubscriptionResponseDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.service.SurveyNotificationSubscriptionService;
import ru.hh.kakdela_v2.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SurveyNotificationSubscriptionService subscriptionService;

    @PostMapping("/surveys/{surveyId}/subscribers")
    @ResponseStatus(HttpStatus.OK)
    public SubscriptionResponseDto subscribeUsers(
            @PathVariable UUID surveyId,
            @RequestBody SubscriptionRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriptionService.subscribeUsers(surveyId, dto.getEmails(), currentUser.getId());
    }

    @DeleteMapping("/surveys/{surveyId}/subscribers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribeUser(
            @PathVariable UUID surveyId,
            @RequestParam String email,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        subscriptionService.unsubscribeUser(surveyId, email, currentUser.getId());
    }

    // TODO: заменить на AccountResponseDto
    @GetMapping("/surveys/{surveyId}/subscribers")
    public List<Account> getSubscribers(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriptionService.getSubscribers(surveyId, currentUser.getId());
    }

    @GetMapping("/surveys/{surveyId}/subscribers/check")
    public boolean isSubscribed(
            @PathVariable UUID surveyId,
            @RequestParam String email,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriptionService.isSubscribed(surveyId, email, currentUser.getId());
    }
}