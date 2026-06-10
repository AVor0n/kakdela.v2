package ru.hh.kakdela_v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionCreateDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela_v2.dto.answer_option.AnswerOptionUpdateDto;
import ru.hh.kakdela_v2.service.AnswerOptionService;
import ru.hh.kakdela_v2.util.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/answer-options")
@RequiredArgsConstructor
public class AnswerOptionController {

    private final AnswerOptionService answerOptionService;

    @GetMapping("/questions/{questionId}/answer-options")
    public List<AnswerOptionResponseDto> getAllByQuestionId(@PathVariable UUID questionId) {
        return answerOptionService.getAllByQuestionId(questionId);
    }

    @PostMapping("/questions/{questionId}/answer-options")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerOptionResponseDto create(
            @PathVariable UUID questionId,
            @Valid @RequestBody AnswerOptionCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return answerOptionService.create(questionId, createDto, currentUser.getId());
    }

    @PutMapping("answer-options/{optionId}")
    public AnswerOptionResponseDto update(
            @PathVariable UUID optionId,
            @Valid @RequestBody AnswerOptionUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return answerOptionService.update(optionId, updateDto, currentUser.getId());
    }

    @DeleteMapping("answer-options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID optionId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        answerOptionService.delete(optionId, currentUser.getId());
    }
}
