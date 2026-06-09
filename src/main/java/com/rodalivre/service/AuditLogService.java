package com.rodalivre.service;

import com.rodalivre.domain.entity.AuditLog;
import com.rodalivre.repository.AuditLogRepository;
import com.rodalivre.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Regex para validar formato de IP (IPv4) e prevenir injeção via X-Forwarded-For
    private static final Pattern IP_PATTERN =
            Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");

    public void logAction(String action, String entity, UUID entityId, String oldValue, String newValue) {
        UUID userId = null;
        try {
            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userId = user.getId();
        } catch (Exception ignored) {}

        String ipAddress = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String rawForwardedFor = request.getHeader("X-Forwarded-For");
                // Sanitizar X-Forwarded-For: extrair apenas o primeiro IP e validar formato
                // Previne spoofing de IP no audit log por clientes maliciosos
                ipAddress = extractSafeIp(rawForwardedFor, request.getRemoteAddr());
            }
        } catch (Exception ignored) {}

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .oldValue(maskIfSensitive(oldValue, entity))
                .newValue(maskIfSensitive(newValue, entity))
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Extrai e valida o IP de origem da requisição.
     * Usa o primeiro IP do header X-Forwarded-For (antes da vírgula) apenas se for um IPv4 válido.
     * Caso contrário, usa remoteAddr como fonte confiável.
     */
    private String extractSafeIp(String xForwardedFor, String remoteAddr) {
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String firstIp = xForwardedFor.split(",")[0].trim();
            if (IP_PATTERN.matcher(firstIp).matches()) {
                return firstIp;
            }
        }
        return remoteAddr;
    }

    private String maskIfSensitive(String value, String entity) {
        if (value == null || value.trim().isEmpty() || "NONE".equalsIgnoreCase(value)) {
            return value;
        }
        if ("User".equalsIgnoreCase(entity) || "Cliente".equalsIgnoreCase(entity)) {
            // Mascara hashes de senhas (geralmente começam com $2a$), CPF e CNH limpos ou com formatos comuns
            if (value.startsWith("$2a$") || value.length() >= 30 || value.matches("\\d{11}") || value.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
                return "[DADO PROTEGIDO POR LGPD]";
            }
        }
        return value;
    }
}
