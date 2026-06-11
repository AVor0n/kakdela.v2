package ru.hh.kakdela_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

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
                @UniqueConstraint(name = "uk_page_survey_serial",
                        columnNames = {"survey_id", "serial_number"})
        }
)
public class SurveyPage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Survey survey;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "title", length = 150, nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @OneToMany(mappedBy = "surveyPage", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Question> questions = new ArrayList<>();
}
