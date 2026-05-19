package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certification_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificationStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificationStatusId;

    @Column(nullable = false, length = 20)
    private String certificationStatus;

    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}