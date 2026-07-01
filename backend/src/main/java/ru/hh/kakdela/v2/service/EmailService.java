package ru.hh.kakdela.v2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    public void sendSurveyPublishedEmail(String email, String surveyTitle, UUID surveyId) {
        log.info("Уведомление о публикации опроса");
        log.info("Кому: {}", email);
        log.info("Опрос: {}", surveyTitle);
        log.info("Ссылка: /surveys/{}", surveyId);
    }
}
