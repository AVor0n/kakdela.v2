package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.hh.kakdela.v2.dto.survey_subscription.SurveySubscriptionRequestDto;
import ru.hh.kakdela.v2.dto.survey_subscription.SurveySubscriptionResponseDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.SurveySubscriptionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Subscribers", description = "Управление подписками на уведомления")
public class SubscriptionController {

  private final SurveySubscriptionService subscriptionService;

  @PostMapping("/surveys/{surveyId}/subscribers")
  @ResponseStatus(HttpStatus.OK)
  public SurveySubscriptionResponseDto subscribeUsers(
      @PathVariable UUID surveyId,
      @RequestBody SurveySubscriptionRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return subscriptionService.subscribeUsers(surveyId, dto, authenticatedUser.getId());
  }

  @DeleteMapping("/surveys/{surveyId}/subscribers")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unsubscribeUser(
      @PathVariable UUID surveyId,
      @RequestParam String email,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    subscriptionService.unsubscribeUser(surveyId, email, authenticatedUser.getId());
  }

  @GetMapping("/surveys/{surveyId}/subscribers")
  public List<AccountResponseDto> getSubscribers(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return subscriptionService.getSubscribers(surveyId, authenticatedUser.getId());
  }

  @GetMapping("/surveys/{surveyId}/subscribers/check")
  public boolean isSubscribed(
      @PathVariable UUID surveyId,
      @RequestParam String email,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser) {
    return subscriptionService.isSubscribed(surveyId, email, authenticatedUser.getId());
  }
}