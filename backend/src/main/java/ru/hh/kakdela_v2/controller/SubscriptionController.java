package ru.hh.kakdela_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.subscription.SubscriptionRequestDto;
import ru.hh.kakdela_v2.service.SurveyNotificationSubscriberService;
import ru.hh.kakdela_v2.util.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SurveyNotificationSubscriberService subscriberService;

    @PostMapping("/surveys/{surveyId}/subscribers")
    @ResponseStatus(HttpStatus.OK)
    public void subscribeUsers(
            @PathVariable UUID surveyId,
            @RequestBody SubscriptionRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        subscriberService.subscribeUsers(surveyId, dto.getUserIds(), currentUser.getId());
    }

    @DeleteMapping("/surveys/{surveyId}/subscribers/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribeUser(
            @PathVariable UUID surveyId,
            @PathVariable UUID accountId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        subscriberService.unsubscribeUser(surveyId, accountId, currentUser.getId());
    }

    @GetMapping("/surveys/{surveyId}/subscribers")
    public List<UUID> getSubscribers(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriberService.getSubscribers(surveyId, currentUser.getId());
    }

    @GetMapping("/surveys/{surveyId}/subscribers/{accountId}/check")
    public boolean isSubscribed(
            @PathVariable UUID surveyId,
            @PathVariable UUID accountId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriberService.isSubscribed(surveyId, accountId, currentUser.getId());
    }
}
