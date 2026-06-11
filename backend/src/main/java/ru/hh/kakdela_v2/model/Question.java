package ru.hh.kakdela_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question",
        indexes = {
                @Index(name = "idx_question_survey_page_id", columnList = "survey_page_id")
        }
)
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_page_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private SurveyPage surveyPage;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private QuestionType type;

  @Column(name = "answer_option_order", nullable = false, columnDefinition = "TEXT")
  @Enumerated(EnumType.STRING)
  private AnswerOptionOrder answerOptionOrder;

  @Column(name = "is_mandatory", nullable = false)
  private boolean isMandatory;

  @Column(name = "is_visible", nullable = false)
  private boolean isVisible;

  @Column(name = "condition", columnDefinition = "TEXT")
  private String condition;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AnswerOption> answerOptions = new ArrayList<>();

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Answer> answers = new ArrayList<>();

  public enum QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    SHORT_TEXT,
    LONG_TEXT
  }

  public enum AnswerOptionOrder {
    ORIGINAL,
    RANDOM
  }
}
