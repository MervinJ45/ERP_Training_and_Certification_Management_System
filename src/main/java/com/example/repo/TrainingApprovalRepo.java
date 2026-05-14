package com.example.repo;

import com.example.entity.TrainingApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingApprovalRepo extends JpaRepository<TrainingApproval, UUID> {

}
