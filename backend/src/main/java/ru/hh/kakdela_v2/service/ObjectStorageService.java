package ru.hh.kakdela_v2.service;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.bucket.name}")
  private String bucketName;

  public void putObject(String key, MultipartFile file, String contentType) {
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType(contentType)
              .build(),
          RequestBody.fromBytes(file.getBytes())
      );
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Ошибка чтения файла",
          e
      );
    }
  }

  public URL generateObjectUrl(String key, Duration ttl) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(ttl)
            .build();

    return s3Presigner.presignGetObject(getObjectPresignRequest).url();
  }

  public void deleteObject(String key) {
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()
    );
  }
}
