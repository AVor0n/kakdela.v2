package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

  @EmbeddedId
  private PermissionId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("accountId")
  @JoinColumn(name = "account_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("surveyId")
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Survey survey;

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  private SurveyRole role;

  @Column(name = "do_notify", nullable = false)
  @Builder.Default
  private boolean doNotify = true;

  @Embeddable
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PermissionId implements Serializable {
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "survey_id")
    private UUID surveyId;
  }

  @AllArgsConstructor
  @Getter
  public enum SurveyRole {
    AUTHOR(true, true),
    EDITOR(true, true),
    ANALYST(true, false);

    private final boolean responseReadAccess;
    private final boolean editAccess;
  }
}
