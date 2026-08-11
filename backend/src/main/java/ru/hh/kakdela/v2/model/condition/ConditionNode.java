package ru.hh.kakdela.v2.model.condition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "condition_node")
public class ConditionNode {

  @Id
  @Column(name = "id")
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
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private ConditionAtom atom;

  @OneToMany(
      mappedBy = "parentNode",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Builder.Default
  private List<ConditionNode> childNodes = new ArrayList<>();

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum Operator {
    AND(true),
    OR(true),
    ATOM(false),
    NOT_ATOM(false);

    public final boolean isLink;
  }
}
