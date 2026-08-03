package ru.hh.kakdela.v2.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import ru.hh.kakdela.v2.security.JwtRequestFilter;
import ru.hh.kakdela.v2.security.OAuth2LoginFailureHandler;
import ru.hh.kakdela.v2.security.OAuth2LoginSuccessHandler;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String OAUTH2_AUTHORIZATION_BASE_URI = "/api/auth/oauth2/authorization";
  private static final String OAUTH2_CALLBACK_BASE_URI = "/api/auth/oauth2/callback/*";

  private final JwtRequestFilter jwtRequestFilter;
  private final CorsConfigurationSource corsConfigurationSource;
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final AuthorizationRequestRepository<OAuth2AuthorizationRequest>
      authorizationRequestRepository;
  @Lazy
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
  private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
      hhTokenResponseClient;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> hhUserService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    DefaultOAuth2AuthorizationRequestResolver resolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, OAUTH2_AUTHORIZATION_BASE_URI);
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
            .accessDeniedHandler((request, response, ex) ->
                response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage())))
        // Отдельно эндпоинты /api/auth/oauth2/** не перечисляем - они уже покрыты
        // существующим правилом .requestMatchers("/api/auth/**").permitAll() выше
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(a -> a
                .baseUri(OAUTH2_AUTHORIZATION_BASE_URI)
                .authorizationRequestRepository(authorizationRequestRepository)
                .authorizationRequestResolver(authorizationRequestResolver))
            .redirectionEndpoint(r -> r.baseUri(OAUTH2_CALLBACK_BASE_URI))
            .tokenEndpoint(t -> t.accessTokenResponseClient(hhTokenResponseClient))
            .userInfoEndpoint(u -> u.userService(hhUserService))
            .successHandler(oAuth2LoginSuccessHandler)
            .failureHandler(oAuth2LoginFailureHandler))
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
