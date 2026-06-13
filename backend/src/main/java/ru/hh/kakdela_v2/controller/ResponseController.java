package ru.hh.kakdela_v2.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.response.ResponseCreateResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela_v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela_v2.service.ResponseService;
import ru.hh.kakdela_v2.util.CustomUserDetails;

import javax.xml.datatype.Duration;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResponseController {

  private final ResponseService responseService;

  // User side

  @PostMapping("/surveys/{surveyId}/responses")
  public ResponseCreateResponseDto create(@PathVariable UUID surveyId,
                                          @AuthenticationPrincipal CustomUserDetails currentUser,
                                          HttpServletResponse response) {
    ResponseWithTokenDto responseWithTokenDto = responseService.create(surveyId,
        (currentUser != null ? currentUser.getId() : null));

    if (responseWithTokenDto.getResponseAccessToken() != null) {

      ResponseCookie responseCompleteTokenCookie = ResponseCookie.from("responseAccessToken",
              responseWithTokenDto.getResponseAccessToken())
          .httpOnly(true)
          .sameSite("strict")
          .path("/api/responses")
          .maxAge(60 * 60 * 24 * 7)
          .build();

      ResponseCookie answerEditTokenCookie = ResponseCookie.from("answerAccessToken",
              responseWithTokenDto.getResponseAccessToken())
          .httpOnly(true)
          .sameSite("strict")
          .path("/api/responses")
          .maxAge(60 * 60 * 24 * 7)
          .build();

      response.addHeader("Set-Cookie", responseCompleteTokenCookie.toString());
      response.addHeader("Set-Cookie", answerEditTokenCookie.toString());
    }

    return new ResponseCreateResponseDto(responseWithTokenDto.getResponseId());
  }

  @PostMapping("/responses/{responseId}/complete")
  public ResponseEntity<?> complete(@PathVariable UUID responseId,
                                    @AuthenticationPrincipal CustomUserDetails currentUser,
                                    @CookieValue(value = "responseAccessToken",
                                        required = false) String token) {
    responseService.complete(responseId, (currentUser != null ? currentUser.getId() : null), token);
    return ResponseEntity.ok("Ответ записан");
  }

  @GetMapping("/responses/{responseId}")
  public ResponseResponseDto getById(@PathVariable UUID responseId,
                                    @AuthenticationPrincipal CustomUserDetails currentUser,
                                    @CookieValue(value = "responseAccessToken",
                                        required = false) String token) {
    return responseService.getById(responseId, (currentUser != null ? currentUser.getId() : null), token);
  }

  @GetMapping("/surveys/{surveyId}/my-incomplete-responses")
  public List<ResponseResponseDto> findIncompleted(@PathVariable UUID surveyId,
                                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getIncompletedBySurveyIdAndAccountId(surveyId, currentUser.getId());
  }

  @GetMapping("/accounts/me/responses")
  public List<ResponseResponseDto> getMyResponses(@AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getAllByAccountId(currentUser.getId());
  }

  // Author side

  @GetMapping("/surveys/{surveyId}/responses")
  public List<ResponseResponseDto> getResponsesBySurvey(@PathVariable UUID surveyId,
                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getCompletedBySurveyId(surveyId, currentUser.getId());
  }

}
