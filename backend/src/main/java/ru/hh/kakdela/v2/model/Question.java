package ru.hh.kakdela.v2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela.v2.dto.answer.option.AnswerOptionResponseDto;

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
        @UniqueConstraint(name = "uk_question_page_serial",
            columnNames = {"survey_page_id", "serial_number"})
    }
)
public class Question {

  @Id
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

  @Column(name = "text", length = 200, nullable = false)
  private String text;

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

  @Column(name = "has_other_option", nullable = false)
  @Getter(AccessLevel.NONE)
  private boolean hasOtherOption;

  public boolean hasOtherOption() {
    return this.hasOtherOption;
  }

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

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum QuestionType {
    SINGLE_CHOICE(true, false, false,
        false, true, false, true),
    MULTIPLE_CHOICE(true, false, false,
        false, true, true, true),
    SHORT_TEXT(true, false, false,
        false, false, false, false),
    LONG_TEXT(true, false, false,
        false, false, false, false),
    YES_NO(false, true, false,
        false, false, false, false),
    DATE(false, false, true,
        false, false, false, false),
    TIME(false, false, false,
        true, false, false, false);

    public final boolean isTextAllowed;
    public final boolean isBooleanAllowed;
    public final boolean isDateAllowed;
    public final boolean isTimeAllowed;
    public final boolean isAnswerOptionsAllowed;
    public final boolean isMultipleChoiceAllowed;
    public final boolean isOtherOptionAllowed;
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum AnswerOptionOrder {
    ORIGINAL(
        aords -> {
          aords.sort(Comparator.comparingInt(AnswerOptionResponseDto::getSerialNumber));
          return aords;
        }),
    RANDOM(
        aords -> {
          Collections.shuffle(aords);
          return aords;
        });

    public final Function<List<AnswerOptionResponseDto>, List<AnswerOptionResponseDto>> function;
  }
}
