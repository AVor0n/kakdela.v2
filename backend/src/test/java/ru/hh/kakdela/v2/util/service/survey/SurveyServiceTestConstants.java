package ru.hh.kakdela.v2.util.service.survey;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class SurveyServiceTestConstants {
  
  public static final UUID account1Id = UUID.randomUUID();
  public static final UUID account2Id = UUID.randomUUID();

  @AllArgsConstructor
  public enum FullSurveyConstants {

    SURVEY(UUID.randomUUID(), UUID.randomUUID()),
    PAGE1(UUID.randomUUID(), UUID.randomUUID()),
    QUESTION1(UUID.randomUUID(), UUID.randomUUID()),
    QUESTION2(UUID.randomUUID(), UUID.randomUUID()),
    QUESTION3(UUID.randomUUID(), UUID.randomUUID()),
    ANSWER_OPTION1_OF_QUESTION2(UUID.randomUUID(), UUID.randomUUID()),
    ANSWER_OPTION2_OF_QUESTION2(UUID.randomUUID(), UUID.randomUUID()),
    ANSWER_OPTION1_OF_QUESTION3(UUID.randomUUID(), UUID.randomUUID()),
    ANSWER_OPTION2_OF_QUESTION3(UUID.randomUUID(), UUID.randomUUID()),
    CLOSING_PAGE(UUID.randomUUID(), UUID.randomUUID());

    private final UUID originalId;
    private final UUID cloneId;

    @Getter
    private static final UUID response1Id = UUID.randomUUID();

    private static final UUID originalAuthorId = account1Id;
    private static final UUID cloneAuthorId = account2Id;

    private static final String originalTitle = "fullSurvey";
    private static final String cloneTitle = "Копия — fullSurvey";

    public UUID getId(boolean isClone) {
      return isClone
          ? this.cloneId
          : this.originalId;
    }

    public static UUID getAuthorId(boolean isClone) {
      return isClone
          ? cloneAuthorId
          : originalAuthorId;
    }

    public static String getTitle(boolean isClone) {
      return isClone
          ? cloneTitle
          : originalTitle;
    }
  }

  public static class PlainSurveyConstants {

    private static final String originalTitle = "plainSurvey";
    private static final String updatedTitle = "plainSurvey — updated";

    private static final String originalDescription = "description";
    private static final String updatedDescription = "description — updated";

    public static String getTitle(boolean isUpdated) {
      return isUpdated
          ? updatedTitle
          : originalTitle;
    }

    public static String getDescription(boolean isUpdated) {
      return isUpdated
          ? updatedDescription
          : originalDescription;
    }
  }
  
  public static final UUID plainSurveyId = UUID.randomUUID();

  public static final String attachmentObjectKey = "attachmentObjectKey";
  public static final String attachmentUrl = "http://attachmentUrl/";

  public static final Instant expireAtSevenDays = Instant.now()
      .plus(7, ChronoUnit.DAYS)
      .truncatedTo(ChronoUnit.SECONDS);
  public static final Instant anotherExpireAtPlusHour = expireAtSevenDays.plus(Duration.ofHours(1));
  public static final LocalDateTime expireAtAtMoscowTimezone =
      atZone(expireAtSevenDays, "Europe/Moscow");
  public static final LocalDateTime expireAtAtYekaterinburgTimezone =
      atZone(expireAtSevenDays, "Asia/Yekaterinburg");
  public static final LocalDateTime anotherExpireAtAtYekaterinburgTimezone =
      atZone(anotherExpireAtPlusHour, "Asia/Yekaterinburg");
  public static final LocalDateTime anotherExpireAtAtKamchatkaTimezone =
      atZone(anotherExpireAtPlusHour, "Asia/Kamchatka");
  // Разница между Asia/Yekateringurg и Asia/Kamchatka составляет +7 часов в любое время года.
  // Значение переменной ниже на 7 часов больше, чем у expireAtSevenDays.
  // При этом её значение в Asia/Kamchatka (UTC+12) равно значению expireAtSevenDays в Asia/Yekateringurg (UTC+5)
  public static final Instant expireAtAfterTargetTimezoneUpdateFromYekaterinburgToKamchatka =
      toInstant(expireAtAtYekaterinburgTimezone,"Asia/Kamchatka");

  private static LocalDateTime atZone(Instant instant, String zoneId) {
    return instant.atZone(ZoneId.of(zoneId)).toLocalDateTime();
  }

  private static Instant toInstant(LocalDateTime localDateTime, String zoneId) {
    return localDateTime.atZone(ZoneId.of(zoneId)).toInstant();
  }
}
