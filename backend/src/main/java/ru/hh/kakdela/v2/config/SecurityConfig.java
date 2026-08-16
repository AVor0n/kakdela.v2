package ru.hh.kakdela.v2.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import ru.hh.kakdela.v2.security.JwtRequestFilter;
import ru.hh.kakdela.v2.security.Oauth2LoginFailureHandler;
import ru.hh.kakdela.v2.security.Oauth2LoginSuccessHandler;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${app.oauth2.authorization-base-uri:/api/auth/oauth2/authorization}")
  private String oauth2AuthorizationBaseUri;
  @Value("${app.oauth2.callback-base-uri:/api/auth/oauth2/callback/*}")
  private String oauth2CallbackBaseUri;

  private final JwtRequestFilter jwtRequestFilter;
  private final CorsConfigurationSource corsConfigurationSource;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
      authorizationRequestRepository;
  private final Oauth2LoginSuccessHandler oauth2LoginSuccessHandler;
  private final Oauth2LoginFailureHandler oauth2LoginFailureHandler;
  private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
      hhTokenResponseClient;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> hhUserService;
  private final AccessDeniedHandler accessDeniedHandler;

  @Bean
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    DefaultOAuth2AuthorizationRequestResolver resolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, oauth2AuthorizationBaseUri);
    // hh.ru поддерживает PKCE (code_challenge/S256) - включаем его даже для confidential-клиента
    resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
    return resolver;
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/monitor/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                    "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/surveys/{surveyId}/my-incompleted-responses").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/surveys/{surveyId}/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/surveys/{surveyId}/responses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/responses/{responseId}").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/responses/{responseId}/complete").permitAll()
                .requestMatchers("/api/responses/{responseId}/answers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pages/{pageId}/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/questions/{questionId}/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/answer-options/{answerOptionId}/**").permitAll()
                .anyRequest().authenticated())
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(a -> a
                .baseUri(oauth2AuthorizationBaseUri)
                .authorizationRequestRepository(authorizationRequestRepository)
                .authorizationRequestResolver(authorizationRequestResolver))
            .redirectionEndpoint(r -> r.baseUri(oauth2CallbackBaseUri))
            .tokenEndpoint(t -> t.accessTokenResponseClient(hhTokenResponseClient))
            .userInfoEndpoint(u -> u.userService(hhUserService))
            .successHandler(oauth2LoginSuccessHandler)
            .failureHandler(oauth2LoginFailureHandler))
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
