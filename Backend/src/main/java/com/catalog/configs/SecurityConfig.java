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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Публичные endpoints
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/services").permitAll()
                .requestMatchers("/api/services/**").permitAll()

                .requestMatchers("/api/subservices").permitAll()
                .requestMatchers("/api/subservices/**").permitAll()

                // Подписание договора — каждая роль свой endpoint
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/client")
                    .hasRole("CLIENT")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/approver")
                    .hasRole("APPROVER")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/financier")
                    .hasRole("FINANCIER")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/director")
                    .hasRole("DIRECTOR")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/sign/gen_director")
                    .hasRole("GEN_DIRECTOR")

                // Отклонение — любая из подписывающих ролей
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/reject")
                    .hasAnyRole("APPROVER", "FINANCIER", "DIRECTOR", "GEN_DIRECTOR")

                // Создание и отправка договора — менеджер
                .requestMatchers(HttpMethod.POST, "/api/contracts/*")
                    .hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/contracts/*/submit")
                    .hasRole("MANAGER")

                // Направление в лабораторию — директор или ген.директор
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/assign-lab")
                    .hasAnyRole("DIRECTOR", "GEN_DIRECTOR")

                // Все остальные — авторизация обязательна
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

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