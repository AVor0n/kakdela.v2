package ru.hh.kakdela_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "survey_notification_settings", indexes = {
    @Index(name = "idx_notification_settings_survey_id", columnList = "survey_id")
})
public class SurveyNotificationSettings {

    @Id
    @Column(name = "survey_id", updatable = false, nullable = false)
    private UUID surveyId;

    @Column(name = "notify_editors", nullable = false)
    @Builder.Default
    private boolean notifyEditors = false;

    @Column(name = "notify_analysts", nullable = false)
    @Builder.Default
    private boolean notifyAnalysts = false;

    @Column(name = "notify_custom_user_ids")
    @Builder.Default
    private List<UUID> notifyCustomUserIds = new ArrayList<>();

}
