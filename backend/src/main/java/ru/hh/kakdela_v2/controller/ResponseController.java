package ru.hh.kakdela_v2.controller;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.service.ResponseService;
import ru.hh.kakdela_v2.util.CustomUserDetails;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResponseController {

  private final ResponseService responseService;

  // User side

  @PostMapping("/surveys/{surveyId}/responses")
  public ResponseResponseDto create(@PathVariable UUID surveyId,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.create(surveyId, (currentUser != null ? currentUser.getId() : null));
  }

  @PostMapping("/responses/{responseId}/complete")
  public ResponseEntity<?> complete(@PathVariable UUID responseId,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
    responseService.complete(responseId, (currentUser != null ? currentUser.getId() : null));
    return ResponseEntity.ok("Ответ записан");
  }

  @GetMapping("/surveys/{surveyId}/my-incomplete-responses")
  public List<ResponseResponseDto> findIncomplete(@PathVariable UUID surveyId,
                                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getIncompleteBySurveyIdAndAccountId(surveyId, currentUser.getId());
  }

  @GetMapping("/accounts/me/responses")
  public List<ResponseResponseDto> getMyResponses(@AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getAllByAccountId(currentUser.getId());
  }
}
