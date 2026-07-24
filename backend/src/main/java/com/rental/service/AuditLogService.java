package com.rental.service;

import com.rental.dto.AuditLogDto;
import com.rental.entity.AuditLog;
import com.rental.entity.Owner;
import com.rental.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Owner admin, String action, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .adminId(admin != null ? admin.getId() : null)
                .adminEmail(admin != null ? admin.getEmail() : "SYSTEM")
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public void log(Owner admin, String action, String details, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log(admin, action, details, ip);
    }

    // ===== THIS IS THE METHOD YOU ASKED FOR =====
    public List<AuditLogDto> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(log -> new AuditLogDto(
                        log.getId(),
                        log.getAdminId(),
                        log.getAdminEmail(),
                        log.getAction(),
                        log.getDetails(),
                        log.getIpAddress(),
                        log.getTimestamp()
                ))
                .collect(Collectors.toList());
    }
}