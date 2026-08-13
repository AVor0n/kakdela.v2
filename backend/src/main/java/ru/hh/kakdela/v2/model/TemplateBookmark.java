package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template_bookmark",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_template",
            columnNames = {"account_id", "template_id"})
    },
    indexes = {
        @Index(name = "idx_bookmark_account_id", columnList = "account_id"),
        @Index(name = "idx_bookmark_template_id", columnList = "template_id")
    })
public class TemplateBookmark {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private Survey template;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
