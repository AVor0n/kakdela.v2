package ru.hh.kakdela.v2.service;

import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

  public void putObject(String key, byte[] fileAsByteArray, String contentType) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build(),
        RequestBody.fromBytes(fileAsByteArray));
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
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build());
  }
}
