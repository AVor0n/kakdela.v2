package ru.hh.kakdela.v2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageCreateDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageUpdateDto;
import ru.hh.kakdela.v2.service.SurveyPageService;
import ru.hh.kakdela.v2.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SurveyPageController {

    private final SurveyPageService surveyPageService;

    @GetMapping("/surveys/{surveyId}/pages")
    public List<SurveyPageResponseDto> getAllBySurveyId(@PathVariable UUID surveyId) {
        return surveyPageService.getAllBySurveyId(surveyId);
    }

    @GetMapping("/pages/{pageId}")
    public SurveyPageResponseDto getById(@PathVariable UUID pageId) {
        return surveyPageService.getById(pageId);
    }

    @PostMapping("/surveys/{surveyId}/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyPageResponseDto create(
            @PathVariable UUID surveyId,
            @Valid @RequestBody SurveyPageCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return surveyPageService.create(surveyId, createDto, currentUser.getId());
    }

    @PutMapping("/pages/{pageId}")
    public SurveyPageResponseDto update(
            @PathVariable UUID pageId,
            @Valid @RequestBody SurveyPageUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return surveyPageService.update(pageId, updateDto, currentUser.getId());
    }

    @DeleteMapping("/pages/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID pageId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        surveyPageService.delete(pageId, currentUser.getId());
    }
}
