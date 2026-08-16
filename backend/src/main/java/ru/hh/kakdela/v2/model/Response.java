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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "response",
    indexes = {
        @Index(name = "idx_response_survey_id", columnList = "survey_id"),
        @Index(name = "idx_response_account_id", columnList = "account_id")})
public class Response {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Survey survey;

  @Column(name = "is_completed", nullable = false)
  @Builder.Default
  private boolean isCompleted = false;

  @Column(name = "received_at")
  private Instant receivedAt;

  @OneToMany(
      mappedBy = "response",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @OrderBy("pageSerialNumber ASC, questionSerialNumber ASC")
  @Builder.Default
  private List<Answer> answers = new ArrayList<>();

  @OneToMany(
      mappedBy = "response",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @Builder.Default
  private List<ResponsePageStatus> pageStatuses = new ArrayList<>();
}
