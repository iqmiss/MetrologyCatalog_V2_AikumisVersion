package com.catalog.configs;

import com.catalog.utils.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Конфигурация Spring Security
// Определяет какие endpoints открыты публично, какие требуют авторизации
// Включает JWT фильтр для проверки токенов
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включает @PreAuthorize на контроллерах
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Отключаем CSRF — не нужен для REST API с JWT
            .csrf(csrf -> csrf.disable())

            // Настраиваем CORS — разрешаем запросы с фронтенда
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Настраиваем авторизацию по endpoints
            .authorizeHttpRequests(auth -> auth
                // Публичные endpoints — доступны без токена
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/services").permitAll()
                .requestMatchers("/api/services/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/client").hasRole("CLIENT")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/director").hasRole("DIRECTOR")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/approve").hasAnyRole("APPROVER")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/reject").hasAnyRole("APPROVER")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/submit").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/contracts/*").hasRole("MANAGER")

                // Все остальные endpoints требуют авторизации
                .anyRequest().authenticated()
            )

            // Stateless сессия — не храним сессии на сервере, только JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Добавляем наш JWT фильтр перед стандартным фильтром аутентификации
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Настройка CORS — разрешаем запросы с React фронтенда
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",
            "https://*.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}