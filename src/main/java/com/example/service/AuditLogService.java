package com.example.service;

import com.example.entity.AuditLog;
import com.example.repo.AuditLogRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepo auditLogRepo;

    public AuditLogService(AuditLogRepo auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepo.findAll();
    }

    public AuditLog saveAuditLog(AuditLog auditLog) {
        return auditLogRepo.save(auditLog);
    }

    public AuditLog getAuditLogById(UUID id) {
        return auditLogRepo.findById(id)
                .orElse(null);
    }
}