package ru.hh.kakdela.v2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jsoup.Jsoup;
import ru.hh.kakdela.v2.constants.DefaultValues;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "survey", indexes = {
    @Index(name = "idx_survey_author_id", columnList = "author_id")
})
public class Survey {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Account author;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  // для изображения на превью опроса
  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;

  @Column(name = "is_authorized_only", nullable = false)
  @Builder.Default
  private boolean isAuthorizedOnly = DefaultValues.IS_AUTHORIZED_ONLY_DEFAULT;

  @Column(name = "is_limited_to_one_response", nullable = false)
  @Builder.Default
  private boolean isLimitedToOneResponse = DefaultValues.IS_LIMITED_TO_ONE_RESPONSE_DEFAULT;

  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean isPublished = false;

  @Column(name = "is_template", nullable = false)
  @Builder.Default
  private boolean isTemplate = false;

  @Column(name = "do_notify", nullable = false)
  @Getter(AccessLevel.NONE)
  @Builder.Default
  private boolean doNotify = DefaultValues.DO_NOTIFY_DEFAULT;

  public boolean doNotify() {
    return this.doNotify;
  }

  @Column(name = "expire_at")
  private Instant expireAt;

  @Column(name = "target_timezone")
  @Builder.Default
  private String targetTimezone = DefaultValues.TARGET_TIMEZONE_DEFAULT;

  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @OneToMany(
      mappedBy = "survey",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @OrderBy("serialNumber ASC")
  @Builder.Default
  private List<SurveyPage> pages = new ArrayList<>();

  @OneToOne(
      mappedBy = "survey",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private ClosingPage closingPage;

  @OneToMany(
      mappedBy = "survey",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @Builder.Default
  private List<Permission> permissions = new ArrayList<>();

  @OneToMany(mappedBy = "survey")
  @Builder.Default
  private List<Response> responses = new ArrayList<>();

  public boolean isAuthor(UUID accountId) {
    return this.getAuthor().getId().equals(accountId);
  }

  public String getTitleAsPlainString() {
    return Jsoup.parseBodyFragment(title).text();
  }

  public String getDescriptionAsPlainString() {
    return Jsoup.parseBodyFragment(description).text();
  }
}
