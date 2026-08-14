package ru.hh.kakdela.v2.model.condition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.hh.kakdela.v2.model.Response;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
    name = "condition_node",
    indexes = {
        @Index(name = "idx_condition_node_parent_node_id", columnList = "parent_node_id")})
public class ConditionNode {

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "condition_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Condition condition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_node_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ConditionNode parentNode;

  @Column(name = "operator", nullable = false)
  @Enumerated(EnumType.STRING)
  private Operator operator;

  @OneToOne(
      mappedBy = "node",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE},
      fetch = FetchType.EAGER)
  private ConditionAtom atom;

  @OneToMany(
      mappedBy = "parentNode",
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE})
  @Builder.Default
  private List<ConditionNode> childNodes = new ArrayList<>();

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum Operator {
    AND(true,
        (cn, r) -> {
          boolean result = true;

          for (ConditionNode child : cn.getChildNodes()) {
            result = result && child.evaluate(r);
          }

          return result;
        }),
    OR(true,
        (cn, r) -> {
          boolean result = false;

          for (ConditionNode child : cn.getChildNodes()) {
            result = result || child.evaluate(r);
          }

          return result;
        }),
    ATOM(false,
        (cn, r) -> cn.getAtom().evaluate(r)),
    NOT_ATOM(false,
        (cn, r) -> !cn.getAtom().evaluate(r));

    public final boolean isLink;
    private final BiFunction<ConditionNode, Response, Boolean> function;

    public boolean apply(ConditionNode cn, Response r) {
      return this.function.apply(cn, r);
    }
  }

  public boolean evaluate(Response response) {
    return operator.apply(this, response);
  }
}
