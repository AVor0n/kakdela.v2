package ru.hh.kakdela.v2.util.service.survey;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SurveyServiceTestIdAndTime {

  public static final UUID account1Id = UUID.fromString("5f456068-7941-4188-b7ec-8d2b2ad38b68");
  public static final UUID account2Id = UUID.fromString("a14a1088-3603-466f-a07e-c187daed72d4");
  public static final UUID fullSurveyId = UUID.fromString("41f26c0d-340f-4958-8ac3-8c5ef2299bdf");
  public static final UUID plainSurveyId = UUID.fromString("9f5fb2af-c1ef-4666-830a-f0452c6c7b67");
  public static final UUID fullSurveyCloneId = UUID.fromString("3569dadd-8ffb-46a9-8d7e-3dd6882da658");
  public static final UUID page1CloneId = UUID.fromString("26f03c3e-3e6d-4c11-88e4-63fc4af7e990");
  public static final UUID question1CloneId = UUID.fromString("b10a74e1-01c3-4c60-ae83-970d20318fee");
  public static final UUID question2CloneId = UUID.fromString("a0fda026-f590-4f6a-8b88-e44805b9a349");
  public static final UUID question3CloneId = UUID.fromString("1d6015f5-02fd-475b-a390-b55a6334b33b");
  public static final UUID answerOption1OfQuestion2CloneId = UUID.fromString("f08209f3-71c2-454d-aaaf-d6d15e7448d5");
  public static final UUID answerOption2OfQuestion2CloneId = UUID.fromString("0fba2523-b47f-424f-a62a-fdb2529dd8cf");
  public static final UUID answerOption1OfQuestion3CloneId = UUID.fromString("b4fa920c-05ca-415a-b17f-dadb4358a1fd");
  public static final UUID answerOption2OfQuestion3CloneId = UUID.fromString("12932aff-06b8-4661-99ae-867129500649");
  public static final UUID closingPage1CloneId = UUID.fromString("0e56a5e6-a219-4dfe-9bc4-80a13cfe06b0");

  public static final Instant expireAt = Instant.now()
      .plus(7, ChronoUnit.DAYS)
      .truncatedTo(ChronoUnit.SECONDS);
  public static final Instant anotherExpireAt = expireAt.plus(Duration.ofHours(1));
  // Разница между Asia/Yekateringurg и Asia/Kamchatka составляет +7 часов в любое время года.
  // Значение переменной ниже на 7 часов больше, чем у expireAt.
  // При этом её значение в Asia/Kamchatka (UTC+12) равно значению expireAt в Asia/Yekateringurg (UTC+5)
  public static final Instant expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka = expireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime()
      .atZone(ZoneId.of("Asia/Kamchatka"))
      .toInstant();
  public static final LocalDateTime expireAtAtMoscowTimezone = expireAt
      .atZone(ZoneId.of("Europe/Moscow"))
      .toLocalDateTime();
  public static final LocalDateTime expireAtAtYekaterinburgTimezone = expireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime();
  public static final LocalDateTime anotherExpireAtAtYekaterinburgTimezone = anotherExpireAt
      .atZone(ZoneId.of("Asia/Yekaterinburg"))
      .toLocalDateTime();
  public static final LocalDateTime anotherExpireAtAtKamchatkaTimezone = anotherExpireAt
      .atZone(ZoneId.of("Asia/Kamchatka"))
      .toLocalDateTime();
}
