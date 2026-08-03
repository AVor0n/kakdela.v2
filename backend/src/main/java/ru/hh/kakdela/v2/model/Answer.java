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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
@Table(name = "answer",
    indexes = {
        @Index(name = "idx_answer_response_id", columnList = "response_id"),
        @Index(name = "idx_answer_question_id", columnList = "question_id")
    }
)
public class Answer {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "response_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Response response;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Question question;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "question_text_snapshot", length = 200, nullable = false)
  private String questionTextSnapshot;

  @Column(name = "text_value", length = 5000)
  private String textValue;

  @Column(name = "boolean_value")
  private Boolean booleanValue;

  @Column(name = "date_value")
  private LocalDate dateValue;

  @Column(name = "time_value")
  private LocalTime timeValue;

  @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("serialNumber ASC")
  @Builder.Default
  private List<SelectedAnswerOption> selectedAnswerOptions = new ArrayList<>();

  public String getAnswerAsString() {
    List<String> answerValues = new ArrayList<>();

    if (selectedAnswerOptions != null && !selectedAnswerOptions.isEmpty()) {
      answerValues.add(selectedAnswerOptions.stream()
          .map(SelectedAnswerOption::getAnswerOptionTextSnapshot)
          .collect(Collectors.joining(", ")));
    }
    if (textValue != null) {
      answerValues.add(textValue);
    }
    if (booleanValue != null) {
      answerValues.add(booleanValue ? "Да" : "Нет");
    }
    if (dateValue != null) {
      answerValues.add(dateValue.toString());
    }
    if (timeValue != null) {
      answerValues.add(timeValue.toString());
    }

    return String.join(", ", answerValues);
  }
}
