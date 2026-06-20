package ru.hh.kakdela_v2.service;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.kakdela_v2.dto.image.ProcessedImage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {

  private final Tika tika;

  @PostConstruct
  public void init() {
    ImageIO.setUseCache(false);
    ImageIO.scanForPlugins();
    log.info(Arrays.toString(ImageIO.getWriterFormatNames()));
    log.info(Arrays.toString(ImageIO.getReaderFormatNames()));
  }

  public ProcessedImage process (MultipartFile file) {
    String failMessage = "Загруженное изображение имеет неподдерживаемый формат или испорчено";

    if (file == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, failMessage);
    }

    if (file.getContentType() != null && !file.getContentType().startsWith("image/")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, failMessage);
    }

    try {
      if (!tika.detect(file.getInputStream()).startsWith("image/")) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, failMessage);
      }

      BufferedImage image = ImageIO.read(file.getInputStream());
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      Thumbnails.of(image)
          .size(1920, 1200)
          .outputQuality(0.85f)
          .outputFormat("webp")
          .toOutputStream(outputStream);

      return new ProcessedImage(
          outputStream.toByteArray(),
          "image/webp");
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, failMessage);
    }
  }
}
