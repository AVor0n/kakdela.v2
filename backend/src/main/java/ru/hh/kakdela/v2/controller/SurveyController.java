package ru.hh.kakdela.v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.service.SurveyService;
import ru.hh.kakdela.v2.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Surveys", description = "Управление опросами")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/surveys/public")
    public List<SurveyShortResponseDto> getPublished() {
        return surveyService.getAllPublished();
    }

    @GetMapping("/accounts/me/surveys")
    public List<SurveyShortResponseDto> getMySurveys(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return surveyService.getMySurveys(currentUser.getId());
    }

    @GetMapping("/accounts/{authorId}/surveys")
    public List<SurveyShortResponseDto> getByAuthor(@PathVariable UUID authorId) {
        return surveyService.getAllByAuthorId(authorId);
    }

    @GetMapping("/surveys/{surveyId}")
    public SurveyResponseDto getById(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return surveyService.getById(surveyId);
    }

    @PostMapping("/surveys")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyResponseDto create(
            @Valid @RequestBody SurveyCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return surveyService.create(currentUser.getId(), createDto);
    }

    @PutMapping("/surveys/{surveyId}")
    public SurveyResponseDto update(
            @PathVariable UUID surveyId,
            @Valid @RequestBody SurveyUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return surveyService.update(surveyId, updateDto, currentUser.getId());
    }

    @PostMapping("/surveys/{surveyId}/clone")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyResponseDto clone(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return surveyService.clone(surveyId, currentUser.getId());
    }

    @DeleteMapping("/surveys/{surveyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        surveyService.delete(surveyId, currentUser.getId());
    }
}
