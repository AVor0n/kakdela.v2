package ru.hh.kakdela.v2.conflict;

import java.util.Objects;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.hh.kakdela.v2.model.Question;

@Getter
@EqualsAndHashCode
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

  public static Literal equals(UUID questionId, Question.QuestionType type, UUID answerOptionId) {
    return new Literal(questionId, type, false, answerOptionId, null);
  }

  public static Literal equals(UUID questionId, Question.QuestionType type, Boolean value) {
    return new Literal(questionId, type, false, null, value);
  }

  public static Literal notEquals(UUID questionId, Question.QuestionType type, UUID answerOptionId) {
    return new Literal(questionId, type, true, answerOptionId, null);
  }

  public static Literal notEquals(UUID questionId, Question.QuestionType type, Boolean value) {
    return new Literal(questionId, type, true, null, value);
  }


  public boolean isAnswerOptionType() {
    return answerOptionId != null;
  }

  public boolean isBooleanType() {
    return booleanValue != null;
  }

  private boolean canHaveMultipleValues() {
    return switch (questionType) {
      case MULTIPLE_CHOICE, SHORT_TEXT, LONG_TEXT, DATE, TIME -> true;
      case SINGLE_CHOICE, YES_NO -> false;
    };
  }

  public boolean contradicts(Literal other) {
    if (!this.questionId.equals(other.questionId)) {
      return false;
    }

    if (this.questionType != other.questionType) {
      return false;
    }

    if (this.isAnswerOptionType() != other.isAnswerOptionType()) {
      return false;
    }

    if (this.questionType == Question.QuestionType.MULTIPLE_CHOICE) {
      return contradictsForMultipleChoice(other);
    }

    if (this.canHaveMultipleValues()) {
      return false;
    }

    return contradictsForSingleValue(other);
  }

  private boolean contradictsForMultipleChoice(Literal other) {
    if (!this.isNegated && !other.isNegated) {
      return false;
    }

    if (this.isNegated && other.isNegated) {
      return false;
    }
    return areValuesEqual(other);
  }

  private boolean contradictsForSingleValue(Literal other) {
    if (!areValuesEqual(other)) {
      return true;
    }
    return this.isNegated != other.isNegated;
  }

  private boolean areValuesEqual(Literal other) {
    if (this.isAnswerOptionType() && other.isAnswerOptionType()) {
      return Objects.equals(this.answerOptionId, other.answerOptionId);
    }
    if (this.isBooleanType() && other.isBooleanType()) {
      return Objects.equals(this.booleanValue, other.booleanValue);
    }
    return false;
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
