package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.account.AccountResponseDto;
import ru.hh.kakdela.v2.dto.subscription.SubscriptionRequestDto;
import ru.hh.kakdela.v2.dto.subscription.SubscriptionResponseDto;
import ru.hh.kakdela.v2.mapper.AccountMapper;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.SurveyNotificationSubscriptionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Subscribers", description = "Управление подписками на уведомления")
public class SubscriptionController {

    private final SurveyNotificationSubscriptionService subscriptionService;

    @PostMapping("/surveys/{surveyId}/subscribers")
    @ResponseStatus(HttpStatus.OK)
    public SubscriptionResponseDto subscribeUsers(
            @PathVariable UUID surveyId,
            @Valid @RequestBody SubscriptionRequestDto dto,
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

    @GetMapping("/surveys/{surveyId}/subscribers")
    public List<AccountResponseDto> getSubscribers(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriptionService.getSubscribers(surveyId, currentUser.getId()).stream()
            .map(AccountMapper::accountToDto)
            .toList();
    }

    @GetMapping("/surveys/{surveyId}/subscribers/check")
    public boolean isSubscribed(
            @PathVariable UUID surveyId,
            @RequestParam String email,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return subscriptionService.isSubscribed(surveyId, email, currentUser.getId());
    }
}