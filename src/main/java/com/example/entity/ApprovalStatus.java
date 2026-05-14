package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "approval_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID approvalStatusId;

    private String approvalStatus;
}