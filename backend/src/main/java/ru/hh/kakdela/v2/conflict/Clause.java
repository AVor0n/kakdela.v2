package ru.hh.kakdela.v2.conflict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class Clause {
  private final Set<Literal> literals;

  private Clause(Collection<Literal> literals) {
    this.literals = Set.copyOf(literals);
  }

  public static Clause of(Literal... literals) {
    return new Clause(Set.of(literals));
  }

  public static Clause empty() {
    return new Clause(Collections.emptySet());
  }

  public boolean isEmpty() {
    return literals.isEmpty();
  }

  public static Clause merge(Clause left, Clause right) {
    Set<Literal> merged = new HashSet<>();
    merged.addAll(left.literals);
    merged.addAll(right.literals);

    Clause result = new Clause(merged);

    if (result.hasContradiction()) {
      return Clause.empty();
    }

    return result;
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
