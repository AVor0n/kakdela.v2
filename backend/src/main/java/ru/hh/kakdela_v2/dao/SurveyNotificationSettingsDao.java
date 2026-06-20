package ru.hh.kakdela_v2.dao;

import ru.hh.kakdela_v2.model.SurveyNotificationSettings;

import java.util.Optional;
import java.util.UUID;

public interface SurveyNotificationSettingsDao {

    Optional<SurveyNotificationSettings> findBySurveyId(UUID surveyId);

    void save(SurveyNotificationSettings settings);

    void update(SurveyNotificationSettings settings);

    void deleteBySurveyId(UUID surveyId);

    boolean existsBySurveyId(UUID surveyId);
}
