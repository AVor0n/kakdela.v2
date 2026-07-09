package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.response.ResponseCreateResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.ResponseService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Responses", description = "Управление ответами на опросы")
public class ResponseController {

  private final ResponseService responseService;

  @Value("${app.tokens.response-access.max-age}")
  private long responseTokenMaxAge;

  // User side

  @PostMapping("/surveys/{surveyId}/responses")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseCreateResponseDto create(@PathVariable UUID surveyId,
                                          @AuthenticationPrincipal
                                          CustomUserDetails authenticatedUser,
                                          HttpServletResponse response) {
    ResponseWithTokenDto responseWithTokenDto = responseService.create(surveyId,
        (authenticatedUser != null ? authenticatedUser.getId() : null));

    if (responseWithTokenDto.getResponseAccessToken() != null) {

      ResponseCookie responseAccessTokenCookie = ResponseCookie.from(
              "responseAccessToken", responseWithTokenDto.getResponseAccessToken())
          .httpOnly(true)
          .sameSite("Strict")
          .path("/api/responses")
          .maxAge(responseTokenMaxAge)
          .build();

      response.addHeader("Set-Cookie", responseAccessTokenCookie.toString());
    }

    return new ResponseCreateResponseDto(responseWithTokenDto.getId());
  }

  @PostMapping("/responses/{responseId}/complete")
  public ResponseResponseDto complete(@PathVariable UUID responseId,
                                      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
                                      @CookieValue(value = "responseAccessToken",
                                          required = false) String token) {
    return responseService.complete(
        responseId, (authenticatedUser != null ? authenticatedUser.getId() : null), token);
  }

  @GetMapping("/responses/{responseId}")
  public ResponseResponseDto getById(@PathVariable UUID responseId,
                                     @AuthenticationPrincipal CustomUserDetails authenticatedUser,
                                     @CookieValue(value = "responseAccessToken",
                                         required = false) String token) {
    return responseService.getById(responseId,
        (authenticatedUser != null ? authenticatedUser.getId() : null), token);
  }

  @GetMapping("/surveys/{surveyId}/my-incomplete-responses")
  public List<ResponseResponseDto> findIncompleted(@PathVariable UUID surveyId,
                                                   @AuthenticationPrincipal
                                                   CustomUserDetails authenticatedUser) {
    return responseService.getIncompletedBySurveyIdAndAccountId(surveyId, authenticatedUser.getId());
  }

  @GetMapping("/accounts/me/responses")
  public List<ResponseResponseDto> getMyResponses(
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return responseService.getAllByAccountId(currentUser.getId());
  }

  // Author side

  @GetMapping("/surveys/{surveyId}/responses")
  public List<ResponseResponseDto> getResponsesBySurvey(@PathVariable UUID surveyId,
                                                        @AuthenticationPrincipal
                                                        CustomUserDetails currentUser) {
    return responseService.getCompletedBySurveyId(surveyId, currentUser.getId());
  }

}
