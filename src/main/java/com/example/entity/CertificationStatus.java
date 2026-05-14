package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "certification_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID certificationStatusId;

    private String certificationStatus;
}