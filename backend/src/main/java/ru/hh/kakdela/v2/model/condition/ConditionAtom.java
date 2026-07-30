package ru.hh.kakdela.v2.model.condition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela.v2.model.Answer;
import ru.hh.kakdela.v2.model.AnswerOption;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Response;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "condition_atom")
public class ConditionAtom {

  @Id
  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "condition_node_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ConditionNode node;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Question question;

  @Column(name = "operator", nullable = false)
  @Enumerated(EnumType.STRING)
  private Operator operator;

  @Column(name = "required_boolean_value")
  Boolean requiredBooleanValue;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "required_answer_option_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private AnswerOption requiredAnswerOption;

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum Operator {
    EQUALS(List.of(
        Question.QuestionType.YES_NO,
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.QuestionType.SINGLE_CHOICE),
        (a, b) -> {
          boolean result;

          if (a instanceof Collection<?>) {
            result = ((Collection<?>) a).contains(b);
          } else {
            result = a.equals(b);
          }

          return result;
        }),
    NOT_EQUALS(List.of(
        Question.QuestionType.YES_NO,
        Question.QuestionType.MULTIPLE_CHOICE,
        Question.QuestionType.SINGLE_CHOICE),
        (a, b) -> {
          boolean result;

          if (a instanceof Collection<?>) {
            result = !((Collection<?>) a).contains(b);
          } else {
            result = !a.equals(b);
          }

          return result;
        });

    public final List<Question.QuestionType> allowedQuestionTypes;
    private final BiFunction<Object, Object, Boolean> function;

    public boolean apply(Object a, Object b) {
      return this.function.apply(a, b);
    }
  }

  public boolean evaluate(Response response) {
    Answer answer = response.getAnswers().stream()
        .filter(a -> a.getQuestion().getId().equals(question.getId()))
        .findFirst().orElseThrow(RuntimeException::new);

    boolean result = true;

    //    if (question.getType().isBooleanAllowed) {
    //      result = this.getOperator()
    //          .apply(answer.getBooleanValue(), requiredBooleanValue);
    //    }
    //
    //    if (requiredAnswerOption != null) {
    //      result = result && this.getOperator()
    //          .apply(
    //              answer.getSelectedAnswerOptions().stream()
    //                  .map(sao -> sao.getAnswerOption().getId())
    //                  .toList(),
    //              requiredAnswerOption.getId());
    //    }

    return result;
  }
}
