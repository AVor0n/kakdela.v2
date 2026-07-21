package ru.hh.kakdela.v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.UUID;

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
  private boolean doNotify;

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
