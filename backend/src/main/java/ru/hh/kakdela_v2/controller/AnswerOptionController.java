package ru.hh.kakdela_v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.service.AnswerOptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/answer-option")
@RequiredArgsConstructor
public class AnswerOptionController {

    private final AnswerOptionService answerOptionService;

    @GetMapping("/question/{questionId}/answer-option")
    public List<AnswerOptionResponseDto> getAllByQuestionId(@PathVariable UUID questionId) {
        return answerOptionService.getAllByQuestionId(questionId);
    }

    @PostMapping("/question/{questionId}/answer-option")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerOptionResponseDto create(
            @PathVariable UUID questionId,
            @Valid @RequestBody AnswerOptionCreateDto createDto,
            @AuthenticationPrincipal Account currentUser
    ) {
        return answerOptionService.create(questionId, createDto, currentUser.getId());
    }

    @DeleteMapping("/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID optionId,
            @AuthenticationPrincipal Account currentUser
    ) {
        answerOptionService.delete(optionId, currentUser.getId());
    }
}