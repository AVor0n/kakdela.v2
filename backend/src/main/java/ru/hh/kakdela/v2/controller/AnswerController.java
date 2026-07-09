package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
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
import ru.hh.kakdela.v2.dto.answer.AnswerCreateDto;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.answer.AnswerUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AnswerService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Answers", description = "Управление ответами на вопросы")
public class AnswerController {

  private final AnswerService answerService;

  @GetMapping("/responses/{responseId}/answers")
  public List<AnswerResponseDto> getAllByResponseId(
      @PathVariable UUID responseId,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.getAllByResponseId(
        responseId,
        authenticatedUser != null ? authenticatedUser.getId() : null,
        token
    );
  }

  @PostMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.CREATED)
  public AnswerResponseDto create(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @Valid @RequestBody AnswerCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.create(
        responseId,
        questionId,
        createDto,
        authenticatedUser != null ? authenticatedUser.getId() : null,
        token
    );
  }

  @PutMapping("/responses/{responseId}/answers")
  public AnswerResponseDto update(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @Valid @RequestBody AnswerUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.update(
        responseId,
        questionId,
        updateDto.getAnswerText(),
        authenticatedUser != null ? authenticatedUser.getId() : null,
        token
    );
  }

  @DeleteMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @AuthenticationPrincipal CustomUserDetails authenticatedUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    answerService.delete(
        responseId,
        questionId,
        authenticatedUser != null ? authenticatedUser.getId() : null,
        token
    );
  }
}
