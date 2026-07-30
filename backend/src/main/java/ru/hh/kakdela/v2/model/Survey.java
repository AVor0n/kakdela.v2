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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jsoup.Jsoup;

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

  @Column(name = "is_authorized_only", nullable = false)
  private boolean isAuthorizedOnly;

  @Column(name = "is_limited_to_one_response", nullable = false)
  private boolean isLimitedToOneResponse;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @Column(name = "is_template", nullable = false)
  private boolean isTemplate;

  @Column(name = "do_notify", nullable = false)
  private boolean doNotify;

  @Column(name = "expire_at")
  private Instant expireAt;

  @Column(name = "target_timezone")
  private String targetTimezone;

  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Permission> permissions = new ArrayList<>();

  @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("serial_number ASC")
  @Builder.Default
  private List<SurveyPage> pages = new ArrayList<>();

  @OneToOne(mappedBy = "survey", cascade = CascadeType.ALL,
      orphanRemoval = true, fetch = FetchType.LAZY)
  private ClosingPage closingPage;

  @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Response> responses = new ArrayList<>();

  public String getTitleAsPlainString() {
    return Jsoup.parseBodyFragment(title).text();
  }

  public String getDescriptionAsPlainString() {
    return Jsoup.parseBodyFragment(description).text();
  }
}