package ru.hh.kakdela.v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question",
    indexes = {
        @Index(name = "idx_question_survey_page_id", columnList = "survey_page_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_question_page_serial",
            columnNames = {"survey_page_id", "serial_number"})
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
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private SurveyPage surveyPage;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "description", length = 5000)
  private String description;

  @Column(name = "attachment_object_key", length = 1024)
  private String attachmentObjectKey;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private QuestionType type;

  @Column(name = "answer_option_order")
  @Enumerated(EnumType.STRING)
  private AnswerOptionOrder answerOptionOrder;

  @Column(name = "is_mandatory", nullable = false)
  private boolean isMandatory;

  @Column(name = "is_visible", nullable = false)
  private boolean isVisible;

  @Column(name = "condition", columnDefinition = "text")
  private String condition;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("serial_number ASC")
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
