package ru.hh.kakdela.v2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.model.Account;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseExportService {

  private final AccountDao accountDao;
  private final SurveyDao surveyDao;
  private final QuestionDao questionDao;

  public byte[] exportResponses(List<ResponseResponseDto> responses) throws IOException {

    Survey survey = surveyDao.findById(responses.getFirst().getSurveyId())
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Опрос не найден"
            )
        );

    List<Question> questions = survey.getPages().stream()
        .map(SurveyPage::getQuestions)
        .flatMap(List::stream)
        .toList();

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Ответы");

      // стили
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dateStyle = createDateStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);

      // загаловки
      Row headerRow = sheet.createRow(0);
      int colIndex = 0;

      // колонка "Пользователь"
      Cell userCell = headerRow.createCell(colIndex++);
      userCell.setCellValue("Пользователь");
      userCell.setCellStyle(headerStyle);

      // колонки вопросов
      Map<UUID, Integer> questionColumnMap = new LinkedHashMap<>();
      for (Question question : questions) {
        Cell cell = headerRow.createCell(colIndex);
        cell.setCellValue(question.getTitle());
        cell.setCellStyle(headerStyle);
        questionColumnMap.put(question.getId(), colIndex);
        ++colIndex;
      }

      // колонка "Дата получения"
      Cell dateHeaderCell = headerRow.createCell(colIndex++);
      dateHeaderCell.setCellValue("Дата получения");
      dateHeaderCell.setCellStyle(headerStyle);

      // зполняем данными
      int rowNum = 1;

      Map<UUID, String> userLoginCache = new HashMap<>();

      for (ResponseResponseDto response : responses) {
        Row row = sheet.createRow(rowNum++);
        int cellNum = 0;

        String login = getUserLogin(response.getAccountId(), userLoginCache);
        row.createCell(cellNum++).setCellValue(login);

        Map<UUID, String> answerMap = new HashMap<>();
        if (response.getAnswers() != null) {
          for (AnswerResponseDto answer : response.getAnswers()) {
            answerMap.put(answer.getQuestionId(), answer.getAnswerText());
          }
        }

        for (Question question : questions) {
          String answer = answerMap.getOrDefault(question.getId(), "");
          Integer columnIndex = questionColumnMap.get(question.getId());
          if (columnIndex != null) {
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(answer);
            cell.setCellStyle(dataStyle);
          }
        }

        Cell dateCell = row.createCell(cellNum);
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            response.getReceivedAt(),
            ZoneId.systemDefault()
        );
        dateCell.setCellValue(dateTime);
        dateCell.setCellStyle(dateStyle);

      }

      // настройка ширины колонок
      for (int i = 0; i < colIndex; i++) {
        sheet.autoSizeColumn(i);
        if (sheet.getColumnWidth(i) > 50 * 256) {
          sheet.setColumnWidth(i, 50 * 256);
        }
        if (i == 0 && sheet.getColumnWidth(i) < 15 * 256) {
          sheet.setColumnWidth(i, 15 * 256);
        }
      }

      sheet.createFreezePane(0, 1);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);

      return outputStream.toByteArray();
    }
  }

  private String getUserLogin(UUID accountId, Map<UUID, String> cache) {
    if (accountId == null) {
      return "Аноним";
    }

    return cache.computeIfAbsent(accountId, id ->
        accountDao.findById(id)
            .map(Account::getLogin)
            .orElse("Неизвестный")
    );
  }

  public byte[] exportResponsesWithFilename(
      List<ResponseResponseDto> responses,
      UUID surveyId,
      Map<String, Object> params
  ) throws IOException {

    byte[] data = exportResponses(responses);

    String surveyTitle = surveyDao.findById(surveyId)
        .map(Survey::getTitle)
        .orElse("опрос");

    String safeFileName = surveyTitle
        .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", "")
        .trim()
        .replace(" ", "_");

    params.put("fileName", safeFileName + ".xlsx");
    params.put("data", data);

    return data;
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();

    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 11);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);

    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);

    style.setWrapText(true);

    return style;
  }

  private CellStyle createDateStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    DataFormat df = workbook.createDataFormat();
    style.setDataFormat(df.getFormat("dd.MM.yyyy HH:mm"));
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
  }

  private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setWrapText(true);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
  }
}
