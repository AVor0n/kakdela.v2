package ru.hh.kakdela.v2.conflict;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.condition.ConditionAtom;

@Getter
public class Literal {
  private final UUID questionId;
  private final Question.QuestionType questionType;
  private final boolean isNegated;
  private final UUID answerOptionId;
  private final Boolean booleanValue;

  private Literal(UUID questionId,
                  Question.QuestionType questionType,
                  boolean isNegated,
                  UUID answerOptionId,
                  Boolean booleanValue) {
    this.questionId = Objects.requireNonNull(questionId, "questionId не должен быть null");
    this.questionType = Objects.requireNonNull(questionType, "questionType не должен быть null");
    this.isNegated = isNegated;
    this.answerOptionId = answerOptionId;
    this.booleanValue = booleanValue;
  }

  public static Literal ofPositive(ConditionAtom atom) {
    return new Literal(
        atom.getQuestion().getId(),
        atom.getQuestion().getType(),
        true,
        atom.getRequiredAnswerOption() != null
            ? atom.getRequiredAnswerOption().getId()
            : null,
        atom.getRequiredBooleanValue());
  }

  public static Literal ofNegative(ConditionAtom atom) {
    return new Literal(
        atom.getQuestion().getId(),
        atom.getQuestion().getType(),
        false,
        atom.getRequiredAnswerOption() != null
            ? atom.getRequiredAnswerOption().getId()
            : null,
        atom.getRequiredBooleanValue());
  }

  public boolean contradicts(Literal other) {
    if (!this.questionId.equals(other.questionId)) {
      return false;
    }

    if (this.questionType.isMultipleChoiceAllowed) {
      return contradictsForMultipleChoice(other);
    }

    return contradictsForSingleChoice(other);
  }

  private boolean contradictsForMultipleChoice(Literal other) {
    if (this.isNegated == other.isNegated) {
      return false;
    }

    return areValuesEqual(other);
  }

  private boolean contradictsForSingleChoice(Literal other) {
    if (!areValuesEqual(other)) {
      return true;
    }

    return this.isNegated != other.isNegated;
  }

  private boolean areValuesEqual(Literal other) {
    if (this.questionType.isAnswerOptionsAllowed
        && this.answerOptionId != other.answerOptionId) {
      return false;
    }

    if (this.questionType.isBooleanAllowed
        && this.booleanValue != other.booleanValue) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    String valueStr = answerOptionId != null
        ? answerOptionId.toString().substring(0, 8)
        : String.valueOf(booleanValue);

    if (isNegated) {
      return String.format("NOT(Q%s == %s)",
          questionId.toString().substring(0, 8),
          valueStr);
    }

    return String.format("Q%s == %s",
        questionId.toString().substring(0, 8),
        valueStr);
  }
}
