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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionCreateDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AnswerOptionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Answer Options", description = "Управление вариантами ответов")
public class AnswerOptionController {

  private final AnswerOptionService answerOptionService;

  @GetMapping("/questions/{questionId}/answer-options")
  public List<AnswerOptionResponseDto> getAllByQuestionId(
      @PathVariable UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return answerOptionService.getAllByQuestionId(
        questionId, currentUser != null ? currentUser.getId() : null);
  }

  @GetMapping("answer-options/{answerOptionId}")
  public AnswerOptionResponseDto getById(
      @PathVariable UUID answerOptionId,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    return answerOptionService.getById(
        answerOptionId, currentUser != null ? currentUser.getId() : null);
  }

  @PostMapping("/questions/{questionId}/answer-options")
  @ResponseStatus(HttpStatus.CREATED)
  public AnswerOptionResponseDto create(
      @PathVariable UUID questionId,
      @Valid @RequestBody AnswerOptionCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return answerOptionService.create(
        questionId, createDto, currentUser != null ? currentUser.getId() : null);
  }

  @PutMapping("answer-options/{answerOptionId}")
  public AnswerOptionResponseDto update(
      @PathVariable UUID answerOptionId,
      @Valid @RequestBody AnswerOptionUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return answerOptionService.update(
        answerOptionId, updateDto, currentUser != null ? currentUser.getId() : null);
  }

  @DeleteMapping("answer-options/{answerOptionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID answerOptionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    answerOptionService.delete(
        answerOptionId, currentUser != null ? currentUser.getId() : null);
  }
}
