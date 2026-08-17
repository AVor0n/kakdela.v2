package ru.hh.kakdela.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.kakdela.v2.dto.condition.ConditionNextPageResponseDto;
import ru.hh.kakdela.v2.dto.condition.ConditionRequestDto;
import ru.hh.kakdela.v2.dto.condition.ConditionResponseDto;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomCreateDto;
import ru.hh.kakdela.v2.dto.condition.atom.ConditionAtomUpdateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeCreateDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeResponseDto;
import ru.hh.kakdela.v2.dto.condition.node.ConditionNodeUpdateDto;
import ru.hh.kakdela.v2.security.CustomUserDetails;
import ru.hh.kakdela.v2.service.AuthCookieService;
import ru.hh.kakdela.v2.service.ConditionNodeService;
import ru.hh.kakdela.v2.service.ConditionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Conditions", description = "Управление условиями ветвления")
public class ConditionController {

  private final ConditionService conditionService;
  private final ConditionNodeService conditionNodeService;
  private final AuthCookieService authCookieService;

  @GetMapping("/pages/{pageId}/conditions")
  public List<ConditionResponseDto> getAllByPageId(
      @PathVariable UUID pageId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionService.getAllByPageId(
        pageId, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @GetMapping("/conditions/{conditionId}")
  public ConditionResponseDto getById(
      @PathVariable UUID conditionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionService.getById(
        conditionId, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @GetMapping("/pages/{pageId}/verify")
  public ConditionNextPageResponseDto verify(
      @PathVariable UUID pageId,
      @RequestParam UUID responseId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      HttpServletRequest request
  ) {
    String token = authCookieService.getResponseToken(request, responseId);

    return conditionService.determineNextPage(
        pageId, responseId, currentUser != null ? currentUser.getId() : null, token);
  }

  @PostMapping("/pages/{pageId}/conditions")
  @ResponseStatus(HttpStatus.CREATED)
  public ConditionResponseDto create(
      @PathVariable UUID pageId,
      @Valid @RequestBody ConditionRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionService.create(
        pageId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @PutMapping("/conditions/{conditionId}")
  public ConditionResponseDto update(
      @PathVariable UUID conditionId,
      @Valid @RequestBody ConditionRequestDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionService.update(
        conditionId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @DeleteMapping("/conditions/{conditionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID conditionId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    conditionService.delete(
        conditionId, currentUser != null ? currentUser.getId() : null);
  }

  @PostMapping("/conditions/{conditionId}/nodes")
  @ResponseStatus(HttpStatus.CREATED)
  public ConditionNodeResponseDto addNode(
      @PathVariable UUID conditionId,
      @Valid @RequestBody ConditionNodeCreateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionNodeService.addNode(
        conditionId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @PutMapping("/nodes/{nodeId}")
  public ConditionNodeResponseDto updateNode(
      @PathVariable UUID nodeId,
      @Valid @RequestBody ConditionNodeUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionNodeService.updateNode(
        nodeId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @PostMapping("/conditions/{conditionId}/atoms")
  @ResponseStatus(HttpStatus.CREATED)
  public ConditionNodeResponseDto addAtom(
      @PathVariable UUID conditionId,
      @Valid @RequestBody ConditionAtomCreateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionNodeService.addAtom(
        conditionId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @PutMapping("/nodes/{nodeId}/atom")
  public ConditionNodeResponseDto updateAtom(
      @PathVariable UUID nodeId,
      @Valid @RequestBody ConditionAtomUpdateDto dto,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    return conditionNodeService.updateAtom(
        nodeId, dto, currentUser.getId() != null ? currentUser.getId() : null);
  }

  @DeleteMapping("/nodes/{nodeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteNode(
      @PathVariable UUID nodeId,
      @AuthenticationPrincipal CustomUserDetails currentUser
  ) {
    conditionNodeService.delete(
        nodeId, currentUser != null ? currentUser.getId() : null);
  }
}
