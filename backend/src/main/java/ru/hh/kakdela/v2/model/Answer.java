package ru.hh.kakdela.v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answer",
    indexes = {
        @Index(name = "idx_answer_question_id", columnList = "question_id")
    }
)
public class Answer {

  @EmbeddedId
  private AnswerId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("responseId")
  @JoinColumn(name = "response_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Response response;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("questionId")
  @JoinColumn(name = "question_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Question question;

  @Column(name = "answer_text", length = 5000, nullable = false)
  private String answerText;

  @Embeddable
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AnswerId implements Serializable {
    @Column(name = "response_id")
    private UUID responseId;

    @Column(name = "question_id")
    private UUID questionId;
  }
}
