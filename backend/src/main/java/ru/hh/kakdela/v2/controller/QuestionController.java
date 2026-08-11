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
import ru.hh.kakdela.v2.dto.question.QuestionCreateDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.QuestionService;

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
