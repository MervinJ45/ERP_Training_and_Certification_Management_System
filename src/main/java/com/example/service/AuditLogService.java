package com.example.service;

import com.example.entity.AuditLog;
import com.example.entity.User;
import com.example.repo.AuditLogRepo;
import com.example.utils.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepo auditLogRepo;
    private final CurrentUserProvider currentUserProvider;

    public AuditLogService(AuditLogRepo auditLogRepo, CurrentUserProvider currentUserProvider) {
        this.auditLogRepo = auditLogRepo;
        this.currentUserProvider = currentUserProvider;
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepo.findAll();
    }


    public AuditLog saveAuditLog(AuditLog auditLog) {
        return auditLogRepo.save(auditLog);
    }


    public AuditLog getAuditLogById(Long id) {
        return auditLogRepo.findById(id).orElse(null);
    }

    public void logAudit(Long recordId, String action, String table, String details) {
        User user = currentUserProvider.getCurrentUser();

        AuditLog log = new AuditLog();
        log.setUser(user);
        if (user != null) {
            log.setRole(user.getRole());
        }
        log.setRecordId(recordId);
        log.setAction(action);
        log.setTableAffected(table);
        log.setChangeDetails(details);
        log.setActionTime(LocalDateTime.now());
        auditLogRepo.save(log);
    }
}