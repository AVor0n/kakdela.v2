package ru.hh.kakdela_v2.validator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageValidator {

  private final Tika tika;

  public boolean isImage(MultipartFile file) {

    if (file == null) {
      return false;
    }

    if (file.getContentType() != null && !file.getContentType().startsWith("image/")) {
      return false;
    }

    try {
      if (!tika.detect(file.getInputStream()).startsWith("image/")) {
        return false;
      }

      BufferedImage image = ImageIO.read(file.getInputStream());
      return image != null;
    } catch (IOException e) {
      return false;
    }
  }
}
