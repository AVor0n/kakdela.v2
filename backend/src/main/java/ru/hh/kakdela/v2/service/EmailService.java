package ru.hh.kakdela.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendSurveyPublishedEmail(String email, String surveyTitle, UUID surveyId) {
        try {
            log.info("Уведомление о публикации опроса");
            log.info("Кому: {}", email);
            log.info("Опрос: {}", surveyTitle);
            log.info("Ссылка: /surveys/{}", surveyId);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Обязательный опрос \"" + surveyTitle + "\"!");
            message.setText("Вам необходимо пройти опрос \"" + surveyTitle + "\". Автор указал вас как обязательного респондента.");

            mailSender.send(message);

            log.info("Письмо успешно отправлено для: {}", email);
        }
        catch (Exception e) {
            log.error("Ошибка при отправке письма для {}: {}", email, e.getMessage(), e);
        }
    }
}
