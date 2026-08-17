package ru.hh.kakdela.v2.util;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import ru.hh.kakdela.v2.constants.ConstraintMessages;
import ru.hh.kakdela.v2.constants.TextValueLengthLimits;
import ru.hh.kakdela.v2.exception.Kd2DataValidationException;

public class DataConstraintUtil {

  public static void checkSerialNumberUpperLimit(int value, int limit) {
    if (value > limit) {
      throw new Kd2DataValidationException(
          "serialNumber", ConstraintMessages.SERIAL_NUMBER_UPPER_LIMIT_VIOLATED + limit);
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
      throw new Kd2DataValidationException(
          fieldName, ConstraintMessages.TEXT_VALUE_UPPER_LENGTH_LIMIT_VIOLATED + limit);
    }
  }

  public static String truncateHtmlToUpperLengthLimit(String html, int limit) {
    Document document = Jsoup.parseBodyFragment(html);

    truncate(document.body(), limit);

    return document.body().html();
  }

  private static int truncate(Element element, int remaining) {
    List<Node> children = new ArrayList<>(element.childNodes());

    for (int i = 0; i < children.size(); i++) {
      Node node = children.get(i);

      if (node instanceof TextNode textNode) {
        String text = textNode.getWholeText();

        if (text.length() <= remaining) {
          remaining -= text.length();
          continue;
        }

        textNode.text(text.substring(0, remaining - 3) + "...");

        for (int j = i + 1; j < children.size(); j++) {
          children.get(j).remove();
        }

        return -1;
      }

      if (node instanceof Element child) {
        int result = truncate(child, remaining);

        if (result < 0) {
          for (int j = i + 1; j < children.size(); j++) {
            children.get(j).remove();
          }

          return -1;
        }

        remaining = result;
      }
    }

    return remaining;
  }
}
