package ru.hh.kakdela.v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import ru.hh.kakdela.v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela.v2.service.QuestionService;
import ru.hh.kakdela.v2.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Управление вопросами")
public class QuestionController {

  private final QuestionService questionService;

  @GetMapping("/pages/{pageId}/questions")
  public List<QuestionResponseDto> getAllByPageId(@PathVariable UUID pageId) {
    return questionService.getAllByPageId(pageId);
  }

  @GetMapping("/questions/{questionId}")
  public QuestionResponseDto getById(@PathVariable UUID questionId) {
    return questionService.getById(questionId);
  }

  @PostMapping("/pages/{pageId}/questions")
  @ResponseStatus(HttpStatus.CREATED)
  public QuestionResponseDto create(
      @PathVariable UUID pageId,
      @Valid @RequestBody QuestionCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return questionService.create(pageId, createDto, currentUser.getId());
  }

  @PostMapping("/questions/{questionId}/clone")
  @ResponseStatus(HttpStatus.CREATED)
  public QuestionResponseDto clone(
      @PathVariable UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return questionService.clone(questionId, currentUser.getId());
  }

  @PutMapping("/questions/{questionId}")
  public QuestionResponseDto update(
      @PathVariable UUID questionId,
      @Valid @RequestBody QuestionUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return questionService.update(questionId, updateDto, currentUser.getId());
  }

  @DeleteMapping("/questions/{questionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID questionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    questionService.delete(questionId, currentUser.getId());
  }
}
