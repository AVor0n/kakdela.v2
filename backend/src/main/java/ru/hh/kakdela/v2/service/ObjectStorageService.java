package ru.hh.kakdela.v2.service;

import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.bucket.name}")
  private String bucketName;

  public void putObject(String key, byte[] fileAsByteArray, String contentType) {
    log.debug("Загрузка объекта в хранилище key={} contentType={} size={}",
        key, contentType, fileAsByteArray.length);
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build(),
        RequestBody.fromBytes(fileAsByteArray));
  }

  public void copyObject(String sourceKey, String destinationKey) {
    log.debug("Копирование объекта в хранилище sourceKey={} destinationKey={}",
        sourceKey, destinationKey);
    s3Client.copyObject(
        CopyObjectRequest.builder()
            .sourceBucket(bucketName)
            .sourceKey(sourceKey)
            .destinationBucket(bucketName)
            .destinationKey(destinationKey)
            .build());
  }

  public URL generateObjectUrl(String key, long maxAgeSeconds) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofSeconds(maxAgeSeconds))
            .build();

    return s3Presigner.presignGetObject(getObjectPresignRequest).url();
  }

  public void deleteObject(String key) {
    log.debug("Удаление объекта из хранилища key={}", key);
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build());
  }

  public Long getFileSize(String key) {
    try {
      HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      HeadObjectResponse response = s3Client.headObject(headObjectRequest);
      return response.contentLength();
    } catch (NoSuchKeyException e) {
      log.warn("Объект не найден: {}", key);
      return null;
    }
  }
}
