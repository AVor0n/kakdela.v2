package ru.hh.kakdela.v2.conflict;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.hh.kakdela.v2.model.Clause;
import ru.hh.kakdela.v2.model.Literal;

@Component
public class ClauseAnalyzer {

  public boolean hasIntersection(Clause clause1, Clause clause2) {
    if (clause1.hasContradiction() || clause2.hasContradiction()) {
      return false;
    }

    return Clause.merge(clause1, clause2).isPresent();
  }

  public boolean hasIntersection(DnfExpression dnf1, DnfExpression dnf2) {
    for (Clause clause1 : dnf1.getClauses()) {
      for (Clause clause2 : dnf2.getClauses()) {
        if (hasIntersection(clause1, clause2)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Literal> findConflictingLiterals(Clause clause1, Clause clause2) {
    List<Literal> conflicts = new ArrayList<>();

    for (Literal lit1 : clause1.getLiterals()) {
      for (Literal lit2 : clause2.getLiterals()) {
        if (lit1.contradicts(lit2)) {
          conflicts.add(lit1);
          conflicts.add(lit2);
        }
      }
    }

    return conflicts;
  }
}
