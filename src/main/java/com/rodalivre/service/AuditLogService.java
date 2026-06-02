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

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

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
                ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                }
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
