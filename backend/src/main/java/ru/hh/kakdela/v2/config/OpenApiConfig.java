package ru.hh.kakdela.v2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Kakdela-v2 API")
            .description("API для системы опросов Kakdela-v2")
            .contact(new Contact()
                .name("Kakdela Team")
                .email("support@kakdela.ru")
                .url("https://kakdela.ru"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")));
  }
}
