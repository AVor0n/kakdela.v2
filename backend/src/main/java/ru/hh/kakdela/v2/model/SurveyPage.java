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
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jsoup.Jsoup;
import ru.hh.kakdela.v2.model.condition.Condition;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "survey_page",
    indexes = {
        @Index(name = "idx_survey_page_survey_id", columnList = "survey_id"),
        @Index(name = "idx_survey_page_survey_id_survey_serial_number",
            columnList = "survey_id, serial_number")},
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_page_survey_serial",
            columnNames = {"survey_id", "serial_number"})})
public class SurveyPage {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @ToString.Exclude
  private Survey survey;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @OneToMany(
      mappedBy = "surveyPage",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @OrderBy("serialNumber ASC")
  @Builder.Default
  private List<Question> questions = new ArrayList<>();

  @OneToMany(
      mappedBy = "surveyPage",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      orphanRemoval = true)
  @Builder.Default
  private List<Condition> conditions = new ArrayList<>();

  public String getTitleAsPlainString() {
    return Jsoup.parseBodyFragment(title).text();
  }

  public String getDescriptionAsPlainString() {
    return Jsoup.parseBodyFragment(description).text();
  }
}
