package ru.hh.kakdela.v2.conflict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class DnfExpression {
  private final List<Clause> clauses;

  private DnfExpression(List<Clause> clauses) {
    this.clauses = Collections.unmodifiableList(new ArrayList<>(clauses));
  }

  public static DnfExpression of(Clause... clauses) {
    return new DnfExpression(Arrays.asList(clauses));
  }

  public static DnfExpression of(List<Clause> clauses) {
    return new DnfExpression(clauses);
  }

  public static DnfExpression empty() {
    return new DnfExpression(List.of());
  }

  public boolean isEmpty() {
    return clauses.isEmpty();
  }

  public DnfExpression and(DnfExpression other) {
    List<Clause> result = new ArrayList<>();

    for (Clause left : this.clauses) {
      for (Clause right : other.clauses) {
        Clause.merge(left, right)
            .ifPresent(result::add);
      }
    }

    return new DnfExpression(result);
  }

  public DnfExpression or(DnfExpression other) {
    List<Clause> result = new ArrayList<>();
    result.addAll(this.clauses);
    result.addAll(other.clauses);
    return new DnfExpression(result);
  }

  public DnfExpression removeContradictions() {
    List<Clause> filtered = clauses.stream()
        .filter(c -> !c.hasContradiction())
        .collect(Collectors.toList());
    return new DnfExpression(filtered);
  }

  @Override
  public String toString() {
    return clauses.stream()
        .map(Clause::toString)
        .collect(Collectors.joining(" OR "));
  }
}
