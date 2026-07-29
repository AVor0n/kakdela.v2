package ru.hh.kakdela.v2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HhOAuth2ClientConfig {

  @Value("${app.oauth2.hh-user-agent}")
  private String hhUserAgent;

  @Bean
  public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> hhTokenResponseClient() {
    RestClient restClient = RestClient.builder()
        .defaultHeader("HH-User-Agent", hhUserAgent)
        .build();

    RestClientAuthorizationCodeTokenResponseClient client =
        new RestClientAuthorizationCodeTokenResponseClient();
    client.setRestClient(restClient);
    return client;
  }

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> hhUserService() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add((req, body, execution) -> {
      req.getHeaders().add("HH-User-Agent", hhUserAgent);
      return execution.execute(req, body);
    });

    DefaultOAuth2UserService service = new DefaultOAuth2UserService();
    service.setRestOperations(restTemplate);
    return service;
  }
}
