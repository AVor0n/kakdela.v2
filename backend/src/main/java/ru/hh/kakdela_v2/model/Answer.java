package ru.hh.kakdela_v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

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
    private Response response;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
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
