package ru.hh.kakdela.v2.config;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins:http://localhost:4173}")
  private String allowedOrigins;

  @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
  private String[] allowedMethods;

  @Value("${cors.allowed-headers:*}")
  private String[] allowedHeaders;

  @Value("${cors.exposed-headers:}")
  private String[] exposedHeaders;

  @Value("${cors.allow-credentials:false}")
  private boolean allowCredentials;

  @Value("${cors.max-age:3600}")
  private long maxAge;

  @PostConstruct
  private void validateCorsConfiguration() {
    if (allowCredentials) {
      List<String> origins = parseOrigins(allowedOrigins);
      if (origins.contains("*")) {
        throw new IllegalStateException(
            "When allowCredentials=true, allowedOrigins cannot contain '*'");
      }
      if (List.of(allowedHeaders).contains("*")) {
        log.warn("Использование '*' с параметром allowCredentials=true "
            + "может вызвать проблемы CORS");
      }
    }
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(parseOrigins(allowedOrigins));

    configuration.setAllowedMethods(List.of(allowedMethods));

    configuration.setAllowedHeaders(List.of(allowedHeaders));

    // configuration.setExposedHeaders(List.of(exposedHeaders)); пока что закомментировано

    configuration.setAllowCredentials(allowCredentials);

    configuration.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  private List<String> parseOrigins(String origins) {
    if (origins == null || origins.isBlank()) {
      return List.of();
    }

    return Arrays.stream(origins.split(","))
        .map(String::trim)
        .toList();
  }
}
