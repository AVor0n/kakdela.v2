package ru.hh.kakdela.v2.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class Clause {
  private final Set<Literal> literals;

  private Clause(Set<Literal> literals) {
    this.literals = Collections.unmodifiableSet(new HashSet<>(literals));
  }

  public static Clause of(Literal... literals) {
    return new Clause(new HashSet<>(Arrays.asList(literals)));
  }

  public static Clause of(Collection<Literal> literals) {
    return new Clause(new HashSet<>(literals));
  }

  public static Optional<Clause> merge(Clause left, Clause right) {
    Set<Literal> merged = new HashSet<>();
    merged.addAll(left.literals);
    merged.addAll(right.literals);

    Clause result = new Clause(merged);

    if (result.hasContradiction()) {
      return Optional.empty();
    }

    return Optional.of(result);
  }

  public boolean hasContradiction() {
    List<Literal> list = new ArrayList<>(literals);

    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        if (list.get(i).contradicts(list.get(j))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return literals.stream()
        .map(Literal::toString)
        .collect(Collectors.joining(" AND "));
  }
}
