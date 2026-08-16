package ru.hh.kakdela.v2.util;

import org.jsoup.Jsoup;
import ru.hh.kakdela.v2.constants.Messages;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.exception.Kd2DataValidationException;

public class DataConstraintUtil {

  public static void checkSerialNumberUpperLimit(int value, int limit) {
    if (value > limit) {
      throw new Kd2DataValidationException(
          "serialNumber", Messages.SERIAL_NUMBER_UPPER_LIMIT_VIOLATED + limit);
    }
  }

  public static void checkTitleLength(String title) {
    checkUpperLengthLimit("title", title, TextValueLengthLimits.TITLE_MAX_LENGTH);
  }

  public static void checkDescriptionLength(String description) {
    checkUpperLengthLimit("description", description, TextValueLengthLimits.DESCRIPTION_MAX_LENGTH);
  }

  public static void checkQuestionTextLength(String questionText) {
    checkUpperLengthLimit("text", questionText, TextValueLengthLimits.QUESTION_TEXT_MAX_LENGTH);
  }

  public static void checkAnswerOptionTextLength(String answerOptionText) {
    checkUpperLengthLimit(
        "text", answerOptionText, TextValueLengthLimits.ANSWER_OPTION_TEXT_MAX_LENGTH);
  }

  public static void checkUpperLengthLimit(String fieldName, String value, int limit) {
    if (value == null) {
      return;
    }

    String cleanedValue = Jsoup.parseBodyFragment(value).text();

    if (cleanedValue.length() > limit) {
      throw new Kd2DataValidationException(fieldName, getMessage(limit));
    }
  }

  private static String getMessage(int limit) {
    String result = Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_1 + limit;
    int limitLastDigit = limit % 10;
    int limitLastTwoDigits = limit % 100;

    if (limitLastTwoDigits != 11 && limitLastDigit == 1) {
      result += Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_2_3;
    } else if (
        limitLastTwoDigits != 11
            && limitLastTwoDigits != 12 && limitLastTwoDigits != 13 && limitLastTwoDigits != 14
            || limitLastDigit == 2 || limitLastDigit == 3 || limitLastDigit == 4) {
      result += Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_2_2;
    } else {
      result += Messages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED_2_1;
    }

    return  result;
  }
}
