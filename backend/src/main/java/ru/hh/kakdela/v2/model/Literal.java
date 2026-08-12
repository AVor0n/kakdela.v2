package ru.hh.kakdela.v2.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Literal {
  private final UUID questionId;
  private final boolean isNegated;
  private final UUID answerOptionId;
  private final Boolean booleanValue;

  private Literal(UUID questionId, boolean isNegated, UUID answerOptionId, Boolean booleanValue) {
    this.questionId = Objects.requireNonNull(questionId, "questionId не должен быть null");
    this.isNegated = isNegated;
    this.answerOptionId = answerOptionId;
    this.booleanValue = booleanValue;

    if ((answerOptionId == null) == (booleanValue == null)) {
      throw new IllegalArgumentException(
          "Необходимо указать ровно одно из полей: answerOptionId или booleanValue"
      );
    }
  }

  public static Literal equals(UUID questionId, UUID answerOptionId) {
    return new Literal(questionId, false, answerOptionId, null);
  }

  public static Literal notEquals(UUID questionId, UUID answerOptionId) {
    return new Literal(questionId, true, answerOptionId, null);
  }

  public static Literal equals(UUID questionId, Boolean value) {
    return new Literal(questionId, false, null, value);
  }

  public static Literal notEquals(UUID questionId, Boolean value) {
    return new Literal(questionId, true, null, value);
  }

  public Optional<UUID> getAnswerOptionId() {
    return Optional.ofNullable(answerOptionId);
  }

  public Optional<Boolean> getBooleanValue() {
    return Optional.ofNullable(booleanValue);
  }

  public boolean isAnswerOptionType() {
    return answerOptionId != null;
  }

  public boolean isBooleanType() {
    return booleanValue != null;
  }

  public boolean contradicts(Literal other) {
    if (!this.questionId.equals(other.questionId)) {
      return false;
    }

    if (this.isAnswerOptionType() != other.isAnswerOptionType()) {
      return false;
    }

    if (this.isNegated != other.isNegated) {
      return areValuesEqual(other);
    }

    if (!this.isNegated && !other.isNegated) {
      return !areValuesEqual(other);
    }

    return false;
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
