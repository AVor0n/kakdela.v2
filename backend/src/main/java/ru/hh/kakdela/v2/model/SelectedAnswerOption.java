package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
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
@Table(name = "selected_answer_option",
    indexes = {
        @Index(name = "idx_selected_answer_option_answer_id", columnList = "answer_id")
    }
)
public class SelectedAnswerOption {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Answer answer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answer_option_id", nullable = false)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private AnswerOption answerOption;

  @Column(name = "serial_number", nullable = false)
  private Integer serialNumber;

  @Column(name = "answer_option_text_snapshot",
      length = 1000,
      nullable = false)
  private String answerOptionTextSnapshot;
}
