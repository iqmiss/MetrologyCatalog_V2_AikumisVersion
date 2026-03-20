package com.catalog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// Утилита для работы с JWT токенами
// Генерирует токен при логине и валидирует при каждом запросе
@Component
public class JwtUtil {

    // Секретный ключ и время жизни токена из application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Генерирует подписанный ключ из секретной строки
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Генерирует JWT токен с данными пользователя
    // Вызывается в AuthController при успешном логине
    public String generateToken(int userId, String email, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Извлекает ID пользователя из токена
    public int getUserId(String token) {
        return Integer.parseInt(getClaims(token).getSubject());
    }

    // Извлекает роль пользователя из токена
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Извлекает email пользователя из токена
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // Проверяет что токен валидный и не истёк
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Парсит и возвращает claims (данные) из токена
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}