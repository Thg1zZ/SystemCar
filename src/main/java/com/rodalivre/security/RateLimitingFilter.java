package com.rodalivre.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> globalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    private static final Pattern IP_PATTERN = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = extractSafeIp(request);
        String path = request.getRequestURI();

        // 1. Rate Limiting Global: 100 req/min por IP
        Bucket globalBucket = globalBuckets.computeIfAbsent(ip, key -> 
            Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(100)
                    .refillIntervally(100, Duration.ofMinutes(1))
                    .build())
                .build()
        );

        if (!globalBucket.tryConsume(1)) {
            sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Muitas requisições. Tente novamente mais tarde.");
            return;
        }

        // 2. Rate Limiting endpoints sensiveis
        if (path.endsWith("/api/v1/auth/login") || path.endsWith("/auth/login")) {
            // Login: maximo 5 tentativas em 10 minutos por IP
            Bucket loginBucket = loginBuckets.computeIfAbsent(ip, key -> 
                Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(10))
                        .build())
                    .build()
            );

            if (!loginBucket.tryConsume(1)) {
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Limite de tentativas de login excedido. Tente novamente em 10 minutos.");
                return;
            }
        } else if (path.endsWith("/api/v1/auth/register") || path.endsWith("/auth/register")) {
            // Registro: maximo 3 tentativas por minuto por IP
            Bucket registerBucket = registerBuckets.computeIfAbsent(ip, key -> 
                Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofMinutes(1))
                        .build())
                    .build()
            );

            if (!registerBucket.tryConsume(1)) {
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Limite de tentativas de cadastro excedido. Tente novamente mais tarde.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractSafeIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String firstIp = xForwardedFor.split(",")[0].trim();
            if (IP_PATTERN.matcher(firstIp).matches()) {
                return firstIp;
            }
        }
        return request.getRemoteAddr();
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"error\":\"%s\"}", message));
    }
}
