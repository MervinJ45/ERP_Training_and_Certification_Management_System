package com.example.repo;

import com.example.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {

}
