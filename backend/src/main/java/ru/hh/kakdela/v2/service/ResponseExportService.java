package ru.hh.kakdela.v2.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.dao.QuestionDao;
import ru.hh.kakdela.v2.dao.SurveyDao;
import ru.hh.kakdela.v2.dto.answer.AnswerResponseDto;
import ru.hh.kakdela.v2.dto.response.ResponseExportDto;
import ru.hh.kakdela.v2.dto.response.ResponseResponseDto;
import ru.hh.kakdela.v2.model.Question;
import ru.hh.kakdela.v2.model.Survey;
import ru.hh.kakdela.v2.model.SurveyPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseExportService {

  private final AccountDao accountDao;
  private final SurveyDao surveyDao;
  private final QuestionDao questionDao;

  public byte[] exportResponses(
      List<ResponseResponseDto> responses,
      Survey survey
  ) throws IOException {

    List<Question> questions = survey.getPages().stream()
        .map(SurveyPage::getQuestions)
        .flatMap(List::stream)
        .toList();

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Ответы");

      // стили
      final CellStyle headerStyle = createHeaderStyle(workbook);
      final CellStyle dateStyle = createDateStyle(workbook);
      final CellStyle dataStyle = createDataStyle(workbook);

      // заголовки
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

      // заполняем данными
      if (!responses.isEmpty()) {
        int rowNum = 1;

        Map<UUID, String> userLoginCache = new HashMap<>();

        for (ResponseResponseDto response : responses) {
          Row row = sheet.createRow(rowNum++);
          int cellNum = 0;

          String login = getUserLogin(
              (response.getAccount() != null) ? response.getAccount().getId() : null,
              userLoginCache
          );
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
      return "Анонимный пользователь";
    }

    return cache.computeIfAbsent(accountId, id ->
        accountDao.findById(id)
            .map(account -> account.getLogin() + " (" + account.getEmail() + ")")
            .orElse("Удалённый пользователь (-)")
    );
  }

  public ResponseExportDto exportResponsesWithFilename(
      List<ResponseResponseDto> responses,
      UUID surveyId
  ) throws IOException {

    Survey survey = surveyDao.findById(surveyId)
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Опрос не найден"
            )
        );

    String safeFileName = survey.getTitle()
        .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", "")
        .trim()
        .replace(" ", "_");

    String encodedFileName = URLEncoder.encode(safeFileName + ".xlsx", StandardCharsets.UTF_8)
        .replace("+", "%20");

    return new ResponseExportDto(
        exportResponses(responses, survey),
        encodedFileName
    );
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 11);
    font.setColor(IndexedColors.WHITE.getIndex());

    CellStyle style = workbook.createCellStyle();

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
