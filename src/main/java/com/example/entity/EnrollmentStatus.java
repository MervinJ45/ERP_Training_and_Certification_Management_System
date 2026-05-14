package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "enrollment_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID enrollmentStatusId;

    private String enrollmentStatus;
}