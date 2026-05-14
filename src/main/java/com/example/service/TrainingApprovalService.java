package com.example.service;

import com.example.entity.TrainingApproval;
import com.example.repo.TrainingApprovalRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TrainingApprovalService {

    private final TrainingApprovalRepo
            trainingApprovalRepo;

    public TrainingApprovalService(
            TrainingApprovalRepo trainingApprovalRepo
    ) {
        this.trainingApprovalRepo =
                trainingApprovalRepo;
    }

    public List<TrainingApproval> getAllApprovals() {
        return trainingApprovalRepo.findAll();
    }

    public TrainingApproval saveApproval(
            TrainingApproval approval
    ) {
        return trainingApprovalRepo.save(approval);
    }

    public void deleteApproval(UUID id) {
        trainingApprovalRepo.deleteById(id);
    }

    public TrainingApproval getApprovalById(UUID id) {
        return trainingApprovalRepo.findById(id)
                .orElse(null);
    }
}