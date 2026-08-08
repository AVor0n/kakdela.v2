package ru.hh.kakdela.v2.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonNullableUtil {

  public static <T> T ifNotNullGetOrElse(JsonNullable<T> jsonNullable, T elseValue) {
    if (jsonNullable.isPresent() && jsonNullable.get() != null) {
      return jsonNullable.get();
    } else {
      return elseValue;
    }
  }

  public static <T> T ifNotNullGetOrElseIfNullFirstOrElseSecond(
      JsonNullable<T> jsonNullable,
      T firstValue,
      T secondValue
  ) {
    if (jsonNullable.isPresent()) {
      if (jsonNullable.get() != null) {
        return jsonNullable.get();
      } else {
        return firstValue;
      }
    } else {
      return secondValue;
    }
  }
}
