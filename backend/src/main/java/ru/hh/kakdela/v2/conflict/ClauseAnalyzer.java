package ru.hh.kakdela.v2.conflict;

public class ClauseAnalyzer {

  public static boolean hasIntersection(Clause clause1, Clause clause2) {
    if (clause1.hasContradiction() || clause2.hasContradiction()) {
      return false;
    }

    return Clause.merge(clause1, clause2).isPresent();
  }

  public static boolean hasIntersection(DnfExpression dnf1, DnfExpression dnf2) {
    for (Clause clause1 : dnf1.getClauses()) {
      for (Clause clause2 : dnf2.getClauses()) {
        if (hasIntersection(clause1, clause2)) {
          return true;
        }
      }
    }
    return false;
  }
}
