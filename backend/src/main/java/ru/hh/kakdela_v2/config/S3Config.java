package ru.hh.kakdela_v2.config;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

  @Value("${cloud.aws.internal-endpoint}")
  private String internalEndpoint;

  @Value("${cloud.aws.public-endpoint}")
  private String publicEndpoint;

  @Value("${cloud.aws.region}")
  private String region;

  @Value("${cloud.aws.credentials.access-key-id}")
  private String accessKeyId;

  @Value("${cloud.aws.credentials.secret-access-key}")
  private String secretAccessKey;

  @Bean
  public S3Client s3client() {

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

    try {
      return S3Client.builder()
          .endpointOverride(new URI(internalEndpoint))
          .region(Region.of(region))
          .credentialsProvider(StaticCredentialsProvider.create(credentials))
          .serviceConfiguration(
              S3Configuration.builder()
                  .pathStyleAccessEnabled(true)
                  .build()
          )
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Неверно указан эндпоинт S3");
    }
  }

  @Bean
  public S3Presigner s3Presigner() {

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

    try {
      return S3Presigner.builder()
          .endpointOverride(new URI(publicEndpoint))
          .region(Region.of(region))
          .credentialsProvider(StaticCredentialsProvider.create(credentials))
          .serviceConfiguration(
              S3Configuration.builder()
                  .pathStyleAccessEnabled(true)
                  .build()
          )
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Неверно указан эндпоинт S3");
    }
  }
}
