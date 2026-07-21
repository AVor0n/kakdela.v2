package ru.hh.kakdela.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.answer_option.AnswerOptionResponseDto;
import ru.hh.kakdela.v2.dto.closing_page.ClosingPageResponseDto;
import ru.hh.kakdela.v2.dto.question.QuestionResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyCreateDto;
import ru.hh.kakdela.v2.dto.survey.SurveyResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyShortResponseDto;
import ru.hh.kakdela.v2.dto.survey.SurveyUpdateDto;
import ru.hh.kakdela.v2.dto.survey_page.SurveyPageResponseDto;
import ru.hh.kakdela.v2.mapper.AnswerOptionMapper;
import ru.hh.kakdela.v2.mapper.ClosingPageMapper;
import ru.hh.kakdela.v2.mapper.QuestionMapper;
import ru.hh.kakdela.v2.mapper.SurveyMapper;
import ru.hh.kakdela.v2.mapper.SurveyPageMapper;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.ClosingPage;
import ru.hh.kakdela.v2.model.Permission;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

  @Mock
  private SurveyDao surveyDao;
  @Mock
  private AccountDao accountDao;
  @Mock
  private PermissionService permissionService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private ObjectStorageService objectStorageService;

  private SurveyService surveyService;

  private static UUID testAccount1Id;
  private static UUID testAccount2Id;
  private static UUID testSurveyId;
  private static UUID testSurvey1Id;
  private static UUID testSurvey1CloneId;
  private static UUID testSurvey2Id;
  private static UUID testSurvey3Id;
  private static UUID clonePage1Id;
  private static UUID cloneQuestion1Id;
  private static UUID cloneQuestion2Id;
  private static UUID cloneQuestion3Id;
  private static UUID cloneAnswerOption1Question2Id;
  private static UUID cloneAnswerOption2Question2Id;
  private static UUID cloneAnswerOption1Question3Id;
  private static UUID cloneAnswerOption2Question3Id;
  private static UUID cloneClosingPage1Id;
  private static Instant expireAt;
  private static Account testAccount1;
  private static Account testAccount2;
  private static Survey testSurvey1;
  private static Survey testSurvey1NoClosingPage;
  private static Survey testSurvey1Clone;
  private static Survey testSurvey1CloneNoClosingPage;
  private static Survey testSurvey2;
  private static Survey testSurvey2ExpireAtChanged;
  private static Survey testSurvey2TargetTimezoneChanged;
  private static Survey testSurvey2UpdatedExceptIsPublished;
  private static Survey testSurvey3;
  private static SurveyResponseDto testSurvey1Dto;
  private static SurveyResponseDto testSurvey1CloneDto;
  private static SurveyResponseDto testSurvey1CloneDtoNoClosingPage;
  private static SurveyResponseDto testSurvey2Dto;
  private static SurveyResponseDto testSurvey2DtoExpireAtChanged;
  private static SurveyResponseDto testSurvey2DtoTargetTimezoneChanged;
  private static SurveyResponseDto testSurvey2DtoUpdatedExceptIsPublished;
  private static SurveyResponseDto testSurvey3Dto;
  private static SurveyShortResponseDto testSurvey1ShortDto;
  private static SurveyShortResponseDto testSurvey2ShortDto;
  private static SurveyCreateDto testSurvey2CreateDto;
  private static SurveyCreateDto testSurvey3CreateDto;
  private static SurveyUpdateDto testSurvey2UpdateDtoEverythingExceptIsPublishedChanged;
  private static SurveyUpdateDto surveyUpdateDtoNothingChanged;
  private static SurveyUpdateDto surveyUpdateDtoExpireAtChanged;
  private static SurveyUpdateDto surveyUpdateDtoTargetTimezoneChanged;
  private static SurveyUpdateDto surveyUpdateDtoIsPublishedChangedToTrue;

  @BeforeEach
  void setUp() {
    AnswerOptionMapper answerOptionMapper = new AnswerOptionMapper(objectStorageService);
    QuestionMapper questionMapper = new QuestionMapper(objectStorageService, answerOptionMapper);
    SurveyPageMapper surveyPageMapper = new SurveyPageMapper(questionMapper);
    ClosingPageMapper closingPageMapper = new ClosingPageMapper(objectStorageService);
    SurveyMapper surveyMapper = new SurveyMapper(surveyPageMapper, closingPageMapper);
    surveyService = new SurveyService(
        surveyDao,
        accountDao,
        permissionService,
        notificationService,
        surveyMapper
    );
  }

  @BeforeAll
  static void setUpData() {
    testAccount1Id = UUID.fromString("5f456068-7941-4188-b7ec-8d2b2ad38b68");
    testAccount2Id = UUID.fromString("a14a1088-3603-466f-a07e-c187daed72d4");
    testSurveyId = UUID.fromString("41f26c0d-340f-4958-8ac3-8c5ef2299bdf");
    testSurvey1Id = UUID.fromString("035ee3c1-3bc4-4f71-b19d-0573322eaf11");
    testSurvey2Id = UUID.fromString("9f5fb2af-c1ef-4666-830a-f0452c6c7b67");
    testSurvey3Id = UUID.fromString("f6886e7d-003e-4798-a115-c93fb331e15d");

    testSurvey1CloneId = UUID.fromString("3569dadd-8ffb-46a9-8d7e-3dd6882da658");
    clonePage1Id = UUID.fromString("26f03c3e-3e6d-4c11-88e4-63fc4af7e990");
    cloneQuestion1Id = UUID.fromString("b10a74e1-01c3-4c60-ae83-970d20318fee");
    cloneQuestion2Id = UUID.fromString("a0fda026-f590-4f6a-8b88-e44805b9a349");
    cloneQuestion3Id = UUID.fromString("1d6015f5-02fd-475b-a390-b55a6334b33b");
    cloneAnswerOption1Question2Id = UUID.fromString("f08209f3-71c2-454d-aaaf-d6d15e7448d5");
    cloneAnswerOption2Question2Id = UUID.fromString("0fba2523-b47f-424f-a62a-fdb2529dd8cf");
    cloneAnswerOption1Question3Id = UUID.fromString("b4fa920c-05ca-415a-b17f-dadb4358a1fd");
    cloneAnswerOption2Question3Id = UUID.fromString("12932aff-06b8-4661-99ae-867129500649");
    cloneClosingPage1Id = UUID.fromString("0e56a5e6-a219-4dfe-9bc4-80a13cfe06b0");

    expireAt = Instant.now()
        .plus(1, ChronoUnit.DAYS)
        .truncatedTo(ChronoUnit.SECONDS);
    Instant expireAtForUpdate = expireAt.plus(Duration.ofHours(1));
    // Разница между Asia/Yekateringurg и Asia/Kamchatka составляет +7 часов в любое время года.
    // Значение переменной ниже на 7 часов больше, чем у expireAt.
    // При этом её значение в Asia/Kamchatka (UTC+12) равно значению expireAt в Asia/Yekateringurg (UTC+5)
    Instant expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka = expireAt
        .atZone(ZoneId.of("Asia/Yekaterinburg"))
        .toLocalDateTime()
        .atZone(ZoneId.of("Asia/Kamchatka"))
        .toInstant();
    LocalDateTime expireAtAtMoscowTimezone = expireAt
        .atZone(ZoneId.of("Europe/Moscow"))
        .toLocalDateTime();
    LocalDateTime expireAtAtYekaterinburgTimezone = expireAt
        .atZone(ZoneId.of("Asia/Yekaterinburg"))
        .toLocalDateTime();
    LocalDateTime expireAtForUpdateAtYekaterinburgTimezone = expireAtForUpdate
        .atZone(ZoneId.of("Asia/Yekaterinburg"))
        .toLocalDateTime();
    LocalDateTime expireAtForUpdateAtKamchatkaTimezone = expireAtForUpdate
        .atZone(ZoneId.of("Asia/Kamchatka"))
        .toLocalDateTime();

    testAccount1 = new Account(
        testAccount1Id,
        "account1",
        null,
        null,
        null,
        null,
        null,
        null
    );
    testAccount2 = new Account(
        testAccount2Id,
        "account2",
        null,
        null,
        null,
        null,
        null,
        null
    );

    // Сущность опроса (Survey) со всеми заполненными дочерними сущностями.
    // Для тестирования findById, clone и delete

    testSurvey1 = new Survey(
        testSurvey1Id,
        testAccount1,
        "survey1",
        "description",
        false,
        false,
        true,
        false,
        false,
        expireAt,
        "Europe/Moscow",
        null,
        List.of(
            new Permission(
                new Permission.PermissionId(testAccount2Id, testSurvey1Id),
                testAccount2,
                testSurvey1NoClosingPage,
                Permission.SurveyRole.EDITOR,
                false
            )
        ),
        new ArrayList<>(),
        new ClosingPage(
            UUID.fromString("8198aea4-3f54-43df-ac27-15d11500a744"),
            testSurvey1NoClosingPage,
            "closingPage",
            "description",
            "attachmentObjectKey",
            "websiteUrl"
        ),
        new ArrayList<>()
    );

    // Сущность опроса (Survey) со всеми заполненными дочерними сущностями,
    // кроме завершающей страницы (ClosingPage). Для тестирования clone

    testSurvey1NoClosingPage = new Survey(
        testSurvey1Id,
        testAccount1,
        "survey1",
        "description",
        false,
        false,
        true,
        false,
        false,
        expireAt,
        "Europe/Moscow",
        null,
        List.of(
            new Permission(
                new Permission.PermissionId(testAccount2Id, testSurvey1Id),
                testAccount2,
                testSurvey1NoClosingPage,
                Permission.SurveyRole.EDITOR,
                false
            )
        ),
        new ArrayList<>(),
        null,
        new ArrayList<>()
    );

    // Копии сущностей сверху, которые должны получится в результате clone.
    // Отличаются всеми id, автором и отсутствием ответов

    testSurvey1Clone = new Survey(
        testSurvey1CloneId,
        testAccount2,
        "Копия — survey1",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Europe/Moscow",
        null,
        Collections.emptyList(),
        new ArrayList<>(),
        new ClosingPage(
            cloneClosingPage1Id,
            testSurvey1Clone,
            "closingPage",
            "description",
            "attachmentObjectKey",
            "websiteUrl"
        ),
        new ArrayList<>()
    );
    testSurvey1CloneNoClosingPage = new Survey(
        testSurvey1CloneId,
        testAccount2,
        "Копия — survey1",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Europe/Moscow",
        null,
        Collections.emptyList(),
        new ArrayList<>(),
        null,
        new ArrayList<>()
    );

    // Сущность опроса для тестирования create и update
    // Дедлайн прохождения заполнен

    testSurvey2 = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    // Копии сущности сверху, отличающиеся некоторыми полями
    // Должны получаться в результате update

    testSurvey2ExpireAtChanged = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAtForUpdate,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );
    testSurvey2TargetTimezoneChanged = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
        "Asia/Kamchatka",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );
    testSurvey2UpdatedExceptIsPublished = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2_updated",
        "description_updated",
        true,
        true,
        false,
        false,
        true,
        expireAtForUpdate,
        "Asia/Kamchatka",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    // Сущность опроса для тестирования create и update
    // Дедлайн прохождения не заполнен

    testSurvey3 = new Survey(
        testSurvey3Id,
        testAccount1,
        "survey3",
        "description",
        false,
        false,
        false,
        false,
        false,
        null,
        "Europe/Moscow",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    // Dto на ответ, соответствующие всем сущностям сверху

    testSurvey1Dto = new SurveyResponseDto(
        testSurvey1Id,
        testAccount1Id,
        "survey1",
        "description",
        false,
        false,
        true,
        false,
        false,
        expireAt,
        expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(
            new SurveyPageResponseDto(
                UUID.fromString("e61ab944-4729-4277-af32-893c0470b442"),
                testSurvey1Id,
                1,
                "surveyPage",
                "description",
                List.of(
                    new QuestionResponseDto(
                        UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb"),
                        1,
                        "question1",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SHORT_TEXT.name(),
                        null,
                        true,
                        true,
                        "condition",
                        Collections.emptyList()
                    ),
                    new QuestionResponseDto(
                        UUID.fromString("1d0bd6fc-6d26-4830-b731-1ef8ad59e7f0"),
                        2,
                        "question2",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SINGLE_CHOICE.name(),
                        Question.AnswerOptionOrder.RANDOM.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                UUID.fromString("8789eec6-6ddf-4d40-af21-f6e52dbe14e0"),
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b"),
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    ),
                    new QuestionResponseDto(
                        UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192"),
                        3,
                        "question3",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.MULTIPLE_CHOICE.name(),
                        Question.AnswerOptionOrder.ORIGINAL.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                UUID.fromString("f17be065-b7ed-42ac-a29a-00cca73d9406"),
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba"),
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    )
                )
            )
        ),
        new ClosingPageResponseDto(
            "closingPage",
            "description",
            "http://attachmentUrl/",
            "websiteUrl"
        )
    );
    testSurvey1CloneDto = new SurveyResponseDto(
        testSurvey1CloneId,
        testAccount2Id,
        "Копия — survey1",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(
            new SurveyPageResponseDto(
                clonePage1Id,
                testSurvey1CloneId,
                1,
                "surveyPage",
                "description",
                List.of(
                    new QuestionResponseDto(
                        cloneQuestion1Id,
                        1,
                        "question1",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SHORT_TEXT.name(),
                        null,
                        true,
                        true,
                        "condition",
                        Collections.emptyList()
                    ),
                    new QuestionResponseDto(
                        cloneQuestion2Id,
                        2,
                        "question2",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SINGLE_CHOICE.name(),
                        Question.AnswerOptionOrder.RANDOM.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                cloneAnswerOption1Question2Id,
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                cloneAnswerOption2Question2Id,
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    ),
                    new QuestionResponseDto(
                        cloneQuestion3Id,
                        3,
                        "question3",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.MULTIPLE_CHOICE.name(),
                        Question.AnswerOptionOrder.ORIGINAL.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                cloneAnswerOption1Question3Id,
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                cloneAnswerOption2Question3Id,
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    )
                )
            )
        ),
        new ClosingPageResponseDto(
            "closingPage",
            "description",
            "http://attachmentUrl/",
            "websiteUrl"
        )
    );
    testSurvey1CloneDtoNoClosingPage = new SurveyResponseDto(
        testSurvey1CloneId,
        testAccount2Id,
        "Копия — survey1",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        expireAtAtMoscowTimezone,
        "Europe/Moscow",
        null,
        List.of(
            new SurveyPageResponseDto(
                clonePage1Id,
                testSurvey1CloneId,
                1,
                "surveyPage",
                "description",
                List.of(
                    new QuestionResponseDto(
                        cloneQuestion1Id,
                        1,
                        "question1",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SHORT_TEXT.name(),
                        null,
                        true,
                        true,
                        "condition",
                        Collections.emptyList()
                    ),
                    new QuestionResponseDto(
                        cloneQuestion2Id,
                        2,
                        "question2",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.SINGLE_CHOICE.name(),
                        Question.AnswerOptionOrder.RANDOM.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                cloneAnswerOption1Question2Id,
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                cloneAnswerOption2Question2Id,
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    ),
                    new QuestionResponseDto(
                        cloneQuestion3Id,
                        3,
                        "question3",
                        "description",
                        "http://attachmentUrl/",
                        Question.QuestionType.MULTIPLE_CHOICE.name(),
                        Question.AnswerOptionOrder.ORIGINAL.name(),
                        true,
                        true,
                        "condition",
                        List.of(
                            new AnswerOptionResponseDto(
                                cloneAnswerOption1Question3Id,
                                1,
                                "answerOption1",
                                "http://attachmentUrl/"
                            ),
                            new AnswerOptionResponseDto(
                                cloneAnswerOption2Question3Id,
                                2,
                                "answerOption2",
                                "http://attachmentUrl/"
                            )
                        )
                    )
                )
            )
        ),
        null
    );
    testSurvey2Dto = new SurveyResponseDto(
        testSurvey2Id,
        testAccount1Id,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        expireAtAtYekaterinburgTimezone,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        null
    );
    testSurvey2DtoExpireAtChanged = new SurveyResponseDto(
        testSurvey2Id,
        testAccount1Id,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAtForUpdate,
        expireAtForUpdateAtYekaterinburgTimezone,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        null
    );
    testSurvey2DtoTargetTimezoneChanged = new SurveyResponseDto(
        testSurvey2Id,
        testAccount1Id,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka,
        expireAtAtYekaterinburgTimezone, // Should not change after target timezone change
        "Asia/Kamchatka",
        null,
        Collections.emptyList(),
        null
    );
    testSurvey2DtoUpdatedExceptIsPublished = new SurveyResponseDto(
        testSurvey2Id,
        testAccount1Id,
        "survey2_updated",
        "description_updated",
        true,
        true,
        false,
        false,
        true,
        expireAtForUpdate,
        expireAtForUpdateAtKamchatkaTimezone,
        "Asia/Kamchatka",
        null,
        Collections.emptyList(),
        null
    );
    testSurvey3Dto = new SurveyResponseDto(
        testSurvey3Id,
        testAccount1Id,
        "survey3",
        "description",
        false,
        false,
        false,
        false,
        false,
        null,
        null,
        "Europe/Moscow",
        null,
        Collections.emptyList(),
        null
    );
    testSurvey1ShortDto = new SurveyShortResponseDto(
        testSurvey1Id,
        "survey1",
        "description",
        true,
        null
    );
    testSurvey2ShortDto = new SurveyShortResponseDto(
        testSurvey2Id,
        "survey2",
        "description",
        false,
        null
    );

    // Dto на создание, соответствующее testSurvey2

    testSurvey2CreateDto = new SurveyCreateDto();
    testSurvey2CreateDto.setTitle("survey2");
    testSurvey2CreateDto.setDescription("description");
    testSurvey2CreateDto.setIsAuthorizedOnly(false);
    testSurvey2CreateDto.setIsLimitedToOneResponse(false);
    testSurvey2CreateDto.setDoNotify(false);
    testSurvey2CreateDto.setExpireAtAtTargetTimezone(expireAtAtYekaterinburgTimezone);
    testSurvey2CreateDto.setTargetTimezone("Asia/Yekaterinburg");

    // Dto на создание, соответствующее testSurvey3

    testSurvey3CreateDto = new SurveyCreateDto();
    testSurvey3CreateDto.setTitle("survey3");
    testSurvey3CreateDto.setDescription("description");
    testSurvey3CreateDto.setIsAuthorizedOnly(false);
    testSurvey3CreateDto.setIsLimitedToOneResponse(false);
    testSurvey3CreateDto.setDoNotify(false);

    // Dto на изменение, соответствующие сущностям, которые должны получится в результате update

    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged = new SurveyUpdateDto();
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setTitle("survey2_updated");
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setDescription("description_updated");
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setIsAuthorizedOnly(true);
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setIsLimitedToOneResponse(true);
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setDoNotify(true);
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setExpireAtAtTargetTimezone(
        expireAtForUpdateAtKamchatkaTimezone);
    testSurvey2UpdateDtoEverythingExceptIsPublishedChanged.setTargetTimezone("Asia/Kamchatka");

    surveyUpdateDtoExpireAtChanged = new SurveyUpdateDto();
    surveyUpdateDtoExpireAtChanged.setExpireAtAtTargetTimezone(
        expireAtForUpdateAtYekaterinburgTimezone);

    surveyUpdateDtoTargetTimezoneChanged = new SurveyUpdateDto();
    surveyUpdateDtoTargetTimezoneChanged.setTargetTimezone("Asia/Kamchatka");

    surveyUpdateDtoNothingChanged = new SurveyUpdateDto();

    surveyUpdateDtoIsPublishedChangedToTrue = new SurveyUpdateDto();
    surveyUpdateDtoIsPublishedChangedToTrue.setIsPublished(true);

    // Заполнение страниц и вопросов для testSurvey1

    SurveyPage testSurveyPage1 = new SurveyPage(
        UUID.fromString("e61ab944-4729-4277-af32-893c0470b442"),
        testSurvey1,
        1,
        "surveyPage",
        "description",
        new ArrayList<>()
    );
    Question testQuestion1 = new Question(
        UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb"),
        testSurveyPage1,
        1,
        "question1",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>()
    );
    Question testQuestion2 = new Question(
        UUID.fromString("1d0bd6fc-6d26-4830-b731-1ef8ad59e7f0"),
        testSurveyPage1,
        2,
        "question2",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion2AnswerOptionList = List.of(
        new AnswerOption(
            UUID.fromString("8789eec6-6ddf-4d40-af21-f6e52dbe14e0"),
            testQuestion2,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b"),
            testQuestion2,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion2.setAnswerOptions(testQuestion2AnswerOptionList);
    Question testQuestion3 = new Question(
        UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192"),
        testSurveyPage1,
        3,
        "question3",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion3AnswerOptionList = List.of(
        new AnswerOption(
            UUID.fromString("f17be065-b7ed-42ac-a29a-00cca73d9406"),
            testQuestion3,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba"),
            testQuestion3,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion3.setAnswerOptions(testQuestion3AnswerOptionList);
    testSurveyPage1.getQuestions().add(testQuestion1);
    testSurveyPage1.getQuestions().add(testQuestion2);
    testSurveyPage1.getQuestions().add(testQuestion3);
    testSurvey1.getPages().add(testSurveyPage1);

    // Заполнение ответов для testSurvey1

    Response testResponse1 = new Response(
        UUID.fromString("1ac091de-05f5-449a-8fd2-e7f54c0a4fe6"),
        testAccount2,
        testSurvey1NoClosingPage,
        false,
        null,
        new ArrayList<>()
    );
    Answer testAnswer1 = new Answer(
        new Answer.AnswerId(testResponse1.getId(), testQuestion1.getId()),
        testResponse1,
        testQuestion1,
        "answer1"
    );
    testQuestion1.getAnswers().add(testAnswer1);
    Answer testAnswer2 = new Answer(
        new Answer.AnswerId(testResponse1.getId(), testQuestion2.getId()),
        testResponse1,
        testQuestion2,
        "answerOption1"
    );
    testQuestion2.getAnswers().add(testAnswer2);
    Answer testAnswer3 = new Answer(
        new Answer.AnswerId(testResponse1.getId(), testQuestion3.getId()),
        testResponse1,
        testQuestion3,
        "answerOption2"
    );
    testQuestion3.getAnswers().add(testAnswer3);
    testResponse1.getAnswers().add(testAnswer1);
    testResponse1.getAnswers().add(testAnswer2);
    testResponse1.getAnswers().add(testAnswer3);
    testSurvey1NoClosingPage.getResponses().add(testResponse1);

    // Заполнение страниц и вопросов для testSurvey1NoClosingPage

    SurveyPage testSurveyPage1NoClosingPage = new SurveyPage(
        UUID.fromString("e61ab944-4729-4277-af32-893c0470b442"),
        testSurvey1NoClosingPage,
        1,
        "surveyPage",
        "description",
        new ArrayList<>()
    );
    Question testQuestion1NoClosingPage = new Question(
        UUID.fromString("deb153eb-6065-4797-9337-a3505a3c33eb"),
        testSurveyPage1NoClosingPage,
        1,
        "question1",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>()
    );
    Question testQuestion2NoClosingPage = new Question(
        UUID.fromString("1d0bd6fc-6d26-4830-b731-1ef8ad59e7f0"),
        testSurveyPage1NoClosingPage,
        2,
        "question2",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion2AnswerOptionListNoClosingPage = List.of(
        new AnswerOption(
            UUID.fromString("8789eec6-6ddf-4d40-af21-f6e52dbe14e0"),
            testQuestion2NoClosingPage,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            UUID.fromString("62252b25-5f79-46d3-8b4c-77d2d1152e2b"),
            testQuestion2NoClosingPage,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion2NoClosingPage.setAnswerOptions(testQuestion2AnswerOptionListNoClosingPage);
    Question testQuestion3NoClosingPage = new Question(
        UUID.fromString("e1e2fbba-d861-45ec-8d20-6c6edb75a192"),
        testSurveyPage1NoClosingPage,
        3,
        "question3",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion3AnswerOptionListNoClosingPage = List.of(
        new AnswerOption(
            UUID.fromString("f17be065-b7ed-42ac-a29a-00cca73d9406"),
            testQuestion3NoClosingPage,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            UUID.fromString("c739ac6b-6b9c-4b87-a88f-67a9ed196bba"),
            testQuestion3NoClosingPage,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion3NoClosingPage.setAnswerOptions(testQuestion3AnswerOptionListNoClosingPage);
    testSurveyPage1NoClosingPage.getQuestions().add(testQuestion1NoClosingPage);
    testSurveyPage1NoClosingPage.getQuestions().add(testQuestion2NoClosingPage);
    testSurveyPage1NoClosingPage.getQuestions().add(testQuestion3NoClosingPage);
    testSurvey1NoClosingPage.getPages().add(testSurveyPage1NoClosingPage);

    // Заполнение ответов для testSurvey1NoClosingPage

    Response testResponse1NoClosingPage = new Response(
        UUID.fromString("1ac091de-05f5-449a-8fd2-e7f54c0a4fe6"),
        testAccount2,
        testSurvey1NoClosingPage,
        false,
        null,
        new ArrayList<>()
    );
    Answer testAnswer1NoClosingPage = new Answer(
        new Answer.AnswerId(testResponse1NoClosingPage.getId(), testQuestion1NoClosingPage.getId()),
        testResponse1NoClosingPage,
        testQuestion1NoClosingPage,
        "answer1"
    );
    testQuestion1NoClosingPage.getAnswers().add(testAnswer1NoClosingPage);
    Answer testAnswer2NoClosingPage = new Answer(
        new Answer.AnswerId(testResponse1NoClosingPage.getId(), testQuestion2NoClosingPage.getId()),
        testResponse1NoClosingPage,
        testQuestion2NoClosingPage,
        "answerOption1"
    );
    testQuestion2NoClosingPage.getAnswers().add(testAnswer2NoClosingPage);
    Answer testAnswer3NoClosingPage = new Answer(
        new Answer.AnswerId(testResponse1NoClosingPage.getId(), testQuestion3NoClosingPage.getId()),
        testResponse1NoClosingPage,
        testQuestion3NoClosingPage,
        "answerOption2"
    );
    testQuestion3NoClosingPage.getAnswers().add(testAnswer3NoClosingPage);
    testResponse1NoClosingPage.getAnswers().add(testAnswer1NoClosingPage);
    testResponse1NoClosingPage.getAnswers().add(testAnswer2NoClosingPage);
    testResponse1NoClosingPage.getAnswers().add(testAnswer3NoClosingPage);
    testSurvey1NoClosingPage.getResponses().add(testResponse1NoClosingPage);

    // Заполнение страниц и вопросов для testSurvey1Clone

    SurveyPage testSurveyPage1Clone1 = new SurveyPage(
        clonePage1Id,
        testSurvey1Clone,
        1,
        "surveyPage",
        "description",
        new ArrayList<>()
    );
    Question testQuestion1Clone1 = new Question(
        cloneQuestion1Id,
        testSurveyPage1Clone1,
        1,
        "question1",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>()
    );
    Question testQuestion2Clone1 = new Question(
        cloneQuestion2Id,
        testSurveyPage1Clone1,
        2,
        "question2",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion2AnswerOptionListClone1 = List.of(
        new AnswerOption(
            cloneAnswerOption1Question2Id,
            testQuestion2Clone1,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            cloneAnswerOption2Question2Id,
            testQuestion2Clone1,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion2Clone1.setAnswerOptions(testQuestion2AnswerOptionListClone1);
    Question testQuestion3Clone1 = new Question(
        cloneQuestion3Id,
        testSurveyPage1Clone1,
        3,
        "question3",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion3AnswerOptionListClone1 = List.of(
        new AnswerOption(
            cloneAnswerOption1Question3Id,
            testQuestion3Clone1,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            cloneAnswerOption2Question3Id,
            testQuestion3Clone1,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion3Clone1.setAnswerOptions(testQuestion3AnswerOptionListClone1);
    testSurveyPage1Clone1.getQuestions().add(testQuestion1Clone1);
    testSurveyPage1Clone1.getQuestions().add(testQuestion2Clone1);
    testSurveyPage1Clone1.getQuestions().add(testQuestion3Clone1);
    testSurvey1Clone.getPages().add(testSurveyPage1Clone1);

    // Заполнение страниц и вопросов для testSurvey1CloneNoClosingPage

    SurveyPage testSurveyPage1Clone2 = new SurveyPage(
        clonePage1Id,
        testSurvey1CloneNoClosingPage,
        1,
        "surveyPage",
        "description",
        new ArrayList<>()
    );
    Question testQuestion1Clone2 = new Question(
        cloneQuestion1Id,
        testSurveyPage1Clone2,
        1,
        "question1",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SHORT_TEXT,
        null,
        true,
        true,
        "condition",
        Collections.emptyList(),
        new ArrayList<>()
    );
    Question testQuestion2Clone2 = new Question(
        cloneQuestion2Id,
        testSurveyPage1Clone2,
        2,
        "question2",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.SINGLE_CHOICE,
        Question.AnswerOptionOrder.RANDOM,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion2AnswerOptionListClone2 = List.of(
        new AnswerOption(
            cloneAnswerOption1Question2Id,
            testQuestion2Clone2,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            cloneAnswerOption2Question2Id,
            testQuestion2Clone2,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion2Clone2.setAnswerOptions(testQuestion2AnswerOptionListClone2);
    Question testQuestion3Clone2 = new Question(
        cloneQuestion3Id,
        testSurveyPage1Clone2,
        3,
        "question3",
        "description",
        "attachmentObjectKey",
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.AnswerOptionOrder.ORIGINAL,
        true,
        true,
        "condition",
        null,
        new ArrayList<>()
    );
    List<AnswerOption> testQuestion3AnswerOptionListClone2 = List.of(
        new AnswerOption(
            cloneAnswerOption1Question3Id,
            testQuestion3Clone2,
            1,
            "answerOption1",
            "attachmentObjectKey"
        ),
        new AnswerOption(
            cloneAnswerOption2Question3Id,
            testQuestion3Clone2,
            2,
            "answerOption2",
            "attachmentObjectKey"
        )
    );
    testQuestion3Clone2.setAnswerOptions(testQuestion3AnswerOptionListClone2);
    testSurveyPage1Clone2.getQuestions().add(testQuestion1Clone2);
    testSurveyPage1Clone2.getQuestions().add(testQuestion2Clone2);
    testSurveyPage1Clone2.getQuestions().add(testQuestion3Clone2);
    testSurvey1CloneNoClosingPage.getPages().add(testSurveyPage1Clone2);
  }

  // getById

  @Test
  void getById_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.getById(testSurvey1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + testSurvey1Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void getById_surveyFound_returnCorrectDto() throws MalformedURLException {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    SurveyResponseDto result = surveyService.getById(testSurvey1Id);
    assertEquals(testSurvey1Dto, result);
  }

  // getMySurveys

  @Test
  void getMySurveys_surveysNotFound_returnEmptyListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(testAccount1Id))
        .thenReturn(Collections.emptyList());

    List<SurveyShortResponseDto> result = surveyService.getMySurveys(testAccount1Id);
    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void getMySurveys_surveysFound_returnCorrectListOfDto() {
    Mockito.when(permissionService.getAccessibleSurveys(testAccount1Id))
        .thenReturn(List.of(testSurvey1, testSurvey2));

    List<SurveyShortResponseDto> result = surveyService.getMySurveys(testAccount1Id);
    assertEquals(List.of(testSurvey1ShortDto, testSurvey2ShortDto), result);
  }

  // create

  @Test
  void create_accountNotFound_throwsException() {
    Mockito.when(accountDao.findById(testAccount1Id))
        .thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> surveyService.create(testAccount1Id, testSurvey2CreateDto));
  }

  @Test
  void create_expireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(testAccount1Id))
        .thenReturn(Optional.of(testAccount1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      survey.setId(testSurvey2Id);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(testAccount1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertEquals(testSurvey2.getExpireAt(), survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(testSurvey2, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(testAccount1Id, testSurvey2CreateDto);
    assertEquals(testSurvey2Dto, result);
  }

  @Test
  void create_noExpireAtSet_createCorrectEntity() {
    Mockito.when(accountDao.findById(testAccount1Id))
        .thenReturn(Optional.of(testAccount1));
    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      survey.setId(testSurvey3Id);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      // Проверка автора
      assertEquals(testAccount1, survey.getAuthor());
      // Созданный опрос не должен быть шаблоном
      assertFalse(survey.isTemplate());
      // Проверка дедлайна прохождения
      assertNull(survey.getExpireAt());

      assertEquals(testSurvey3, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.create(testAccount1Id, testSurvey3CreateDto);
    assertEquals(testSurvey3Dto, result);
  }

  // update

  @Test
  void update_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.update(testSurvey2Id, surveyUpdateDtoNothingChanged, testAccount1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + testSurvey2Id + "\"",
        ex.getMessage()
    );

    Mockito.verify(permissionService)
        .checkAccess(testSurvey2Id, testAccount1Id, Permission.SurveyRole.EDITOR);
  }

  @Test
  void update_surveyFound_checkPermissions() {
    // Подготовка данных
    Survey testSurvey2ToUpdate = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2ToUpdate));

    surveyService.update(
        testSurvey2Id,
        surveyUpdateDtoNothingChanged,
        testAccount1Id
    );

    Mockito.verify(permissionService)
        .checkAccess(testSurvey2Id, testAccount1Id, Permission.SurveyRole.EDITOR);
  }

  @Test
  void update_nothingChanged_returnSameSurveyDto() {
    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(testSurvey2, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        testSurvey2Id,
        surveyUpdateDtoNothingChanged,
        testAccount1Id
    );
    assertEquals(testSurvey2Dto, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(testSurvey2Id);
  }

  @Test
  void update_isPublishedChangedToTrueButSurveyAlreadyPublished_doNotSendNotificationToSurveyParticipants() {
    // Подготовка данных
    Survey testSurveyToUpdate = new Survey(
        testSurveyId,
        testAccount1,
        "survey",
        "description",
        false,
        false,
        true,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurveyId))
        .thenReturn(Optional.of(testSurveyToUpdate));

    surveyService.update(
        testSurveyId,
        surveyUpdateDtoIsPublishedChangedToTrue,
        testAccount1Id
    );

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(testSurveyId);
  }

  @Test
  void update_isPublishedChangedToTrue_sendNotificationToSurveyParticipants() {
    // Подготовка данных
    Survey testSurvey2ToUpdate = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2ToUpdate));

    surveyService.update(
        testSurvey2Id,
        surveyUpdateDtoIsPublishedChangedToTrue,
        testAccount1Id
    );

    Mockito.verify(notificationService)
        .sendSurveyPublishedNotifications(testSurvey2Id);
  }

  @Test
  void update_expireAtChanged_convertNewExpireAtToUtcCorrectly() {
    // Подготовка данных
    Survey testSurvey2ToUpdate = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2ToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка дедлайна прохождения
      assertEquals(testSurvey2ExpireAtChanged.getExpireAt(), survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(testSurvey2ExpireAtChanged, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        testSurvey2Id,
        surveyUpdateDtoExpireAtChanged,
        testAccount1Id
    );

    // Дедлайн прохождения должен быть правильно конвертирован в указанный часовой пояс
    assertEquals(testSurvey2DtoExpireAtChanged.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone());

    assertEquals(testSurvey2DtoExpireAtChanged, result);
  }

  // В тесте ниже дедлайн прохождения изменяется из-за смены часового пояса.
  // Его новое значение (в UTC) должно иметь такое же значение при конвертации в новый часовой пояс,
  // как старое имело при конвертации в старый часовой пояс

  @Test
  void update_targetTimezoneChanged_updateExpireAt() {
    // Data prepare
    Survey testSurvey2ToUpdate = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2ToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      // Проверка дедлайна прохождения
      assertEquals(testSurvey2TargetTimezoneChanged.getExpireAt(),
          survey.getExpireAt());
      assertEquals(0, survey.getExpireAt().toEpochMilli() % 1000);

      assertEquals(testSurvey2TargetTimezoneChanged, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        testSurvey2Id,
        surveyUpdateDtoTargetTimezoneChanged,
        testAccount1Id
    );

    // Часовой пояс был изменён с Asia/Yekateringburg на Asia/Kamchatka,
    // но значение времени при конвертации в новый часовой пояс должна остаться тем же,
    // что и старое значение времени при конвертации в старый часовой пояс
    assertEquals(testSurvey2DtoTargetTimezoneChanged.getExpireAtAtTargetTimezone(),
        result.getExpireAtAtTargetTimezone());

    assertEquals(testSurvey2DtoTargetTimezoneChanged, result);
  }

  @Test
  void update_everythingExceptIsPublishedChanged_updateEntityCorrectly() {
    // Data prepare
    Survey testSurvey2ToUpdate = new Survey(
        testSurvey2Id,
        testAccount1,
        "survey2",
        "description",
        false,
        false,
        false,
        false,
        false,
        expireAt,
        "Asia/Yekaterinburg",
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        null,
        Collections.emptyList()
    );

    Mockito.when(surveyDao.findById(testSurvey2Id))
        .thenReturn(Optional.of(testSurvey2ToUpdate));

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      assertEquals(testSurvey2UpdatedExceptIsPublished, survey);

      return null;
    }).when(surveyDao).update(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.update(
        testSurvey2Id,
        testSurvey2UpdateDtoEverythingExceptIsPublishedChanged,
        testAccount1Id
    );
    assertEquals(testSurvey2DtoUpdatedExceptIsPublished, result);

    Mockito.verify(notificationService, never())
        .sendSurveyPublishedNotifications(testSurvey2Id);
  }

  // clone

  @Test
  void clone_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(testSurvey1Id, testAccount2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + testSurvey1Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_accountNotFound_throwException() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));
    Mockito.when(accountDao.findById(testAccount2Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.clone(testSurvey1Id, testAccount2Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Аккаунт не найден: " + testAccount2Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void clone_surveyFound_checkPermissions() throws MalformedURLException {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));
    Mockito.when(accountDao.findById(testAccount2Id))
        .thenReturn(Optional.of(testAccount2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    surveyService.clone(testSurvey1Id, testAccount2Id);

    Mockito.verify(permissionService)
        .checkAccess(testSurvey1Id, testAccount2Id, Permission.SurveyRole.EDITOR);
  }

  @Test
  void clone_surveyWithClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));
    Mockito.when(accountDao.findById(testAccount2Id))
        .thenReturn(Optional.of(testAccount2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      survey.setId(testSurvey1CloneId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      SurveyPage page = survey.getPages().getFirst();
      page.setId(clonePage1Id);

      page.getQuestions().getFirst().setId(cloneQuestion1Id);

      Question question2 = page.getQuestions().get(1);
      question2.setId(cloneQuestion2Id);
      question2.getAnswerOptions().get(0).setId(cloneAnswerOption1Question2Id);
      question2.getAnswerOptions().get(1).setId(cloneAnswerOption2Question2Id);

      Question question3 = page.getQuestions().get(2);
      question3.setId(cloneQuestion3Id);
      question3.getAnswerOptions().get(0).setId(cloneAnswerOption1Question3Id);
      question3.getAnswerOptions().get(1).setId(cloneAnswerOption2Question3Id);

      survey.getClosingPage().setId(cloneClosingPage1Id);

      assertEquals(testSurvey1Clone, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.clone(testSurvey1Id, testAccount2Id);
    assertEquals(testSurvey1CloneDto, result);
  }

  @Test
  void clone_surveyWithoutClosingPage_createCorrectCloneEntity() throws MalformedURLException {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1NoClosingPage));
    Mockito.when(accountDao.findById(testAccount2Id))
        .thenReturn(Optional.of(testAccount2));
    Mockito.when(objectStorageService.generateObjectUrl(Mockito.any(), Mockito.anyLong()))
        .thenReturn(URI.create("http://attachmentUrl/").toURL());

    Mockito.doAnswer(invocation -> {
      Survey survey = invocation.getArgument(0);

      survey.setId(testSurvey1CloneId);

      // Проверка времени создания
      assertTrue(!survey.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(1)))
          && !survey.getCreatedAt().isAfter(Instant.now()));
      assertEquals(0, survey.getCreatedAt().toEpochMilli() % 1000);

      survey.setCreatedAt(null);

      SurveyPage page = survey.getPages().getFirst();
      page.setId(clonePage1Id);

      page.getQuestions().getFirst().setId(cloneQuestion1Id);

      Question question2 = page.getQuestions().get(1);
      question2.setId(cloneQuestion2Id);
      question2.getAnswerOptions().get(0).setId(cloneAnswerOption1Question2Id);
      question2.getAnswerOptions().get(1).setId(cloneAnswerOption2Question2Id);

      Question question3 = page.getQuestions().get(2);
      question3.setId(cloneQuestion3Id);
      question3.getAnswerOptions().get(0).setId(cloneAnswerOption1Question3Id);
      question3.getAnswerOptions().get(1).setId(cloneAnswerOption2Question3Id);

      assertEquals(testSurvey1CloneNoClosingPage, survey);

      return null;
    }).when(surveyDao).save(Mockito.any(Survey.class));

    SurveyResponseDto result = surveyService.clone(testSurvey1Id, testAccount2Id);
    assertEquals(testSurvey1CloneDtoNoClosingPage, result);
  }

  // delete

  @Test
  void delete_surveyNotFound_throwException() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.empty());

    Exception ex = assertThrows(
        ResponseStatusException.class,
        () -> surveyService.delete(testSurvey1Id, testAccount1Id)
    );
    assertEquals(
        "404 NOT_FOUND \"Опрос не найден: " + testSurvey1Id + "\"",
        ex.getMessage()
    );
  }

  @Test
  void delete_surveyFound_checkPermissions() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));

    surveyService.delete(testSurvey1Id, testAccount1Id);

    Mockito.verify(permissionService)
        .checkOwnership(testSurvey1Id, testAccount1Id);
  }

  @Test
  void delete_surveyFound_callDaoDeleteMethod() {
    Mockito.when(surveyDao.findById(testSurvey1Id))
        .thenReturn(Optional.of(testSurvey1));

    surveyService.delete(testSurvey1Id, testAccount1Id);

    Mockito.verify(surveyDao).delete(testSurvey1);
  }

}
