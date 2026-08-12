package ru.hh.kakdela.v2.conflict;

public class ClauseAnalyzer {

  public static boolean hasIntersection(DnfExpression dnf1, DnfExpression dnf2) {
    for (Clause clause1 : dnf1.getClauses()) {
      for (Clause clause2 : dnf2.getClauses()) {
        if (Clause.merge(clause1, clause2).isPresent()) {
          return true;
        }
      }
    }
    return false;
  }
}
