package ru.hh.kakdela.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseExportDto {
  byte[] file;
  String filename;
}
