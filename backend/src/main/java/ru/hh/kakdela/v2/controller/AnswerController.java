package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.constants.CookieNames;
import ru.hh.kakdela.v2.dto.answer.AnswerCreateDto;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.answer.AnswerUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AnswerService;
import ru.hh.kakdela.v2.util.CookieUtil;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Answers", description = "Управление ответами на вопросы")
public class AnswerController {

  private final AnswerService answerService;

  @GetMapping("/responses/{responseId}/answers")
  public List<AnswerResponseDto> getAllByResponseId(
      @PathVariable UUID responseId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    return answerService.getAllByResponseId(
        responseId,
        currentUser != null ? currentUser.getId() : null,
        CookieUtil.getCookieValueByName(
            request, CookieNames.responseAccessTokenPrefix + responseId)
    );
  }

  @PostMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.CREATED)
  public AnswerResponseDto create(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @Valid @RequestBody AnswerCreateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    return answerService.create(
        responseId,
        questionId,
        dto,
        currentUser != null ? currentUser.getId() : null,
        CookieUtil.getCookieValueByName(
            request, CookieNames.responseAccessTokenPrefix + responseId)
    );
  }

  @PutMapping("/responses/{responseId}/answers")
  public AnswerResponseDto update(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @Valid @RequestBody AnswerUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    return answerService.update(
        responseId,
        questionId,
        dto.getAnswerText(),
        currentUser != null ? currentUser.getId() : null,
        CookieUtil.getCookieValueByName(
            request, CookieNames.responseAccessTokenPrefix + responseId)
    );
  }

  @DeleteMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {

    answerService.delete(
        responseId,
        questionId,
        currentUser != null ? currentUser.getId() : null,
        CookieUtil.getCookieValueByName(
            request, CookieNames.responseAccessTokenPrefix + responseId)
    );
  }
}
