package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.constants.CookieNames;
import ru.hh.kakdela.v2.dto.response.ResponseCreateResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseWithTokenDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.ResponseService;
import ru.hh.kakdela.v2.util.CookieUtil;

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
  public ResponseCreateResponseDto create(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletResponse response
  ) {

    ResponseWithTokenDto responseWithTokenDto = responseService.create(surveyId,
        currentUser != null ? currentUser.getId() : null);

    if (responseWithTokenDto.getResponseAccessToken() != null) {

      CookieUtil.setHttpOnlySameSiteStrictCookie(
          response,
          "/api/responses",
          responseTokenMaxAge,
          CookieNames.responseAccessTokenPrefix + responseWithTokenDto.getId(),
          responseWithTokenDto.getResponseAccessToken()
      );
    }

    return new ResponseCreateResponseDto(responseWithTokenDto.getId());
  }

  @PostMapping("/responses/{responseId}/complete")
  public ResponseResponseDto complete(
      @PathVariable UUID responseId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request,
      HttpServletResponse response
  ) {

    final String token = CookieUtil.getCookieValueByName(
        request, CookieNames.responseAccessTokenPrefix + responseId);

    ResponseResponseDto responseDto = responseService.complete(
        responseId, currentUser != null ? currentUser.getId() : null, token);

    if (token != null) {

      CookieUtil.setHttpOnlySameSiteStrictCookie(
          response, "/api/responses", 0,
          CookieNames.responseAccessTokenPrefix + responseId);
    }

    return responseDto;
  }

  @GetMapping("/responses/{responseId}")
  public ResponseResponseDto getById(
      @PathVariable UUID responseId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    return responseService.getById(
        responseId,
        currentUser != null ? currentUser.getId() : null,
        CookieUtil.getCookieValueByName(
            request, CookieNames.responseAccessTokenPrefix + responseId)
    );
  }

  @GetMapping("/surveys/{surveyId}/my-incompleted-responses")
  public List<ResponseResponseDto> findIncompleted(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    return responseService.getIncompletedBySurveyIdAndAccountId(surveyId, currentUser.getId());
  }

  @GetMapping("/accounts/me/responses")
  public List<ResponseResponseDto> getMyResponses(
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {

    return responseService.getAllByAccountId(currentUser.getId());
  }

  // Author side

  @GetMapping("/surveys/{surveyId}/responses")
  public List<ResponseResponseDto> getResponsesBySurvey(
      @PathVariable UUID surveyId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {

    return responseService.getCompletedBySurveyId(surveyId, currentUser.getId());
  }

}
