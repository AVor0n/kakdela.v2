package ru.hh.kakdela_v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela_v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela_v2.model.Account;
import ru.hh.kakdela_v2.service.SurveyPageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/page")
@RequiredArgsConstructor
public class SurveyPageController {

    private final SurveyPageService surveyPageService;

    @GetMapping("/survey/{surveyId}/page")
    public List<SurveyPageResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
        return surveyPageService.getAllBySurveyId(surveyId);
    }

    @GetMapping("/{pageId}")
    public SurveyPageResponseDto getById(@PathVariable UUID pageId) {
        return surveyPageService.getById(pageId);
    }

    @PostMapping("/survey/{surveyId}/page")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyPageResponseDto create(
            @PathVariable UUID surveyId,
            @Valid @RequestBody SurveyPageCreateDto createDto,
            @AuthenticationPrincipal Account currentUser
    ) {
        return surveyPageService.create(surveyId, createDto, currentUser.getId());
    }

    @PutMapping("/{pageId}")
    public SurveyPageResponseDto update(
            @PathVariable UUID pageId,
            @Valid @RequestBody SurveyPageUpdateDto updateDto,
            @AuthenticationPrincipal Account currentUser
    ) {
        return surveyPageService.update(pageId, updateDto, currentUser.getId());
    }

    @DeleteMapping("/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID pageId,
            @AuthenticationPrincipal Account currentUser
    ) {
        surveyPageService.delete(pageId, currentUser.getId());
    }
}