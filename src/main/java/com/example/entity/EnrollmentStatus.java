package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "enrollment_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EnrollmentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentStatusId;

    @Column(nullable = false, length = 20)
    private String enrollmentStatus;

    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}