package ru.hh.kakdela_v2.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import ru.hh.kakdela_v2.filter.JwtRequestFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtRequestFilter jwtRequestFilter;
  private final CorsConfigurationSource corsConfigurationSource;


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(
            auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/surveys/{surveyId}/my-incomplete-responses").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/surveys/{surveyId}/**").permitAll()
                .requestMatchers("/api/surveys/{surveyId}/responses").permitAll()
                .requestMatchers("/api/responses/{responseId}/answers").permitAll()
                .requestMatchers("/api/responses/{responseId}/complete").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/responses/{responseId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pages/{pageId}/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/questions/{questionId}/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/answer-options/{answerOptionId}/**").permitAll()
                .anyRequest().authenticated()
        );
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
