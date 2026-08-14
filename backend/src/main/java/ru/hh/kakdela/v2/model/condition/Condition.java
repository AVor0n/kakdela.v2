package ru.hh.kakdela.v2.model.condition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela.v2.model.Response;
import ru.hh.kakdela.v2.model.SurveyPage;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
    name = "condition",
    indexes = {
        @Index(name = "idx_condition_survey_page_id", columnList = "survey_page_id")},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_condition_page_next_page",
            columnNames = {"survey_page_id", "next_page_id"})})
public class Condition {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_page_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage surveyPage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "next_page_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage nextPage;

  @OneToOne(fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE})
  @JoinColumn(name = "root_node_id", unique = true)
  private ConditionNode root;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = false;

  public boolean evaluate(Response response) {
    return root != null && root.evaluate(response);
  }
}
