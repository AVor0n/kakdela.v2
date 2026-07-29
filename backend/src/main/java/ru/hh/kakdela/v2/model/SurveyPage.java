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
@Table(name = "survey_page",
    indexes = {
        @Index(name = "idx_survey_page_survey_id", columnList = "survey_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_page_survey_serial",
            columnNames = {"survey_id", "serial_number"})
    }
)
public class SurveyPage {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Survey survey;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "description", length = 5000)
  private String description;

  @OneToMany(mappedBy = "surveyPage", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("serial_number ASC")
  @Builder.Default
  private List<Question> questions = new ArrayList<>();

  public String getTitleAsPlainString() {
    return Jsoup.parseBodyFragment(title).text();
  }

  public String getDescriptionAsPlainString() {
    return Jsoup.parseBodyFragment(description).text();
  }
}
