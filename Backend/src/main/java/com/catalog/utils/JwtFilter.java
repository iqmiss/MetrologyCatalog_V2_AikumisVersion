package com.catalog.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Фильтр который перехватывает каждый HTTP запрос
// Проверяет JWT токен из заголовка Authorization
// Если токен валидный — устанавливает аутентификацию в SecurityContext
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Читаем заголовок Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                // Извлекаем данные пользователя из токена
                String role = jwtUtil.getRole(token);
                int userId = jwtUtil.getUserId(token);

                // Создаём объект аутентификации с ролью пользователя
                // Роль должна начинаться с ROLE_ для Spring Security
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );

                // Устанавливаем аутентификацию в контекст Spring Security
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Передаём запрос дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }
}