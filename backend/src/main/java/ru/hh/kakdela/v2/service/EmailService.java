package ru.hh.kakdela.v2.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.mail.from}")
  private String fromEmail;

  @Value("${app.url}")
  private String url;

  @Async
  public void sendSurveyPublishedEmail(String email, String surveyTitle, UUID surveyId) {
    try {
      log.info("Уведомление о публикации опроса");
      logEmailSending(email, surveyTitle, surveyId);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(email);
      message.setSubject("Обязательный опрос \"" + surveyTitle + "\"!");
      message.setText(
          "Вам необходимо пройти опрос \""
              + surveyTitle
              + "\". Автор указал вас как обязательного респондента."
              + "\n Ссылка на опрос: " + url + "/surveys/" + surveyId
      );

      mailSender.send(message);

      log.info("Письмо успешно отправлено для: {}", email);
    } catch (Exception e) {
      log.error("Ошибка при отправке письма для {}: {}", email, e.getMessage(), e);
    }
  }

  @Async
  public void sendIncompletedResponseEmail(String email, String surveyTitle, UUID surveyId) {
    try {
      log.info("Уведомление о незавершённом опросе");
      logEmailSending(email, surveyTitle, surveyId);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(email);
      message.setSubject("Незавершённый опрос \"" + surveyTitle + "\"!");
      message.setText(
          "У вас есть незавершённый опрос \""
              + surveyTitle
              + "\". Не забудьте пройти его."
              + "\n Ссылка на опрос: " + url + "/surveys/" + surveyId
      );

      mailSender.send(message);

      log.info("Отправлено напоминание для: {}", email);
    } catch (Exception e) {
      log.error("Ошибка при отправке письма для {}: {}", email, e.getMessage(), e);
    }
  }

  private void logEmailSending(String email, String surveyTitle, UUID surveyId) {
    log.info("Кому: {}", email);
    log.info("Опрос: {}", surveyTitle);
    log.info("Ссылка: /surveys/{}", surveyId);
  }
}
