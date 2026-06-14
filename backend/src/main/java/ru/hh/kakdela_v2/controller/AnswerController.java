package ru.hh.kakdela_v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.answer.AnswerCreateDto;
import ru.hh.kakdela_v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela_v2.dto.answer.AnswerUpdateDto;
import ru.hh.kakdela_v2.service.AnswerService;
import ru.hh.kakdela_v2.util.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

  private final AnswerService answerService;

  @GetMapping("/responses/{responseId}/answers")
  public List<AnswerResponseDto> getAllByResponseId(
      @PathVariable UUID responseId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.getAllByResponseId(
        responseId,
        currentUser != null ? currentUser.getId() : null,
        token
    );
  }

  @PostMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.CREATED)
  public AnswerResponseDto create(
      @PathVariable UUID responseId,
      @Valid @RequestBody AnswerCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.create(
        responseId,
        createDto,
        currentUser != null ? currentUser.getId() : null,
        token
    );
  }

  @PutMapping("/responses/{responseId}/answers")
  public AnswerResponseDto update(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @Valid @RequestBody AnswerUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    return answerService.update(
        responseId,
        questionId,
        updateDto.getAnswerText(),
        currentUser != null ? currentUser.getId() : null,
        token
    );
  }

  @DeleteMapping("/responses/{responseId}/answers")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID responseId,
      @RequestParam UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @CookieValue(value = "responseAccessToken", required = false) String token
  ) {
    answerService.delete(
        responseId,
        questionId,
        currentUser != null ? currentUser.getId() : null,
        token
    );
  }
}
