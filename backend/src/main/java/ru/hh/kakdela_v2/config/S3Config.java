package ru.hh.kakdela_v2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

  @Value("${cloud.aws.credentials.access-key-id}")
  private String accessKeyId;

  @Value("${cloud.aws.credentials.secret-access-key}")
  private String secretAccessKey;

  @Bean
  public S3Client s3client() {

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

    return S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }
}
