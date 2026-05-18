package com.example.service;

import com.example.dto.TrainingTypeDTO;
import com.example.entity.TrainingType;
import com.example.repo.TrainingTypeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepo trainingTypeRepo;
    private final AuditLogService auditLogService;

    // Constructor injection for both Repository and Audit Log Service
    public TrainingTypeService(TrainingTypeRepo trainingTypeRepo, AuditLogService auditLogService) {
        this.trainingTypeRepo = trainingTypeRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingTypeDTO> getAllTrainingTypeDTOs() {
        return trainingTypeRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveTrainingType(TrainingTypeDTO dto) {
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingType(dto.getTrainingType());
        trainingType.setIsActive(dto.getIsActive());

        TrainingType savedType = trainingTypeRepo.save(trainingType);

        // Track creation in audit trail
        auditLogService.logAudit(
                savedType.getTrainingTypeId(),
                "CREATE_TRAINING_TYPE",
                "TRAINING_TYPES",
                "Created type: " + savedType.getTrainingType()
        );
    }

    @Transactional
    public void updateTrainingType(TrainingTypeDTO dto) {
        TrainingType trainingType = trainingTypeRepo.findById(dto.getTrainingTypeId())
                .orElseThrow(() -> new RuntimeException("Training Type Not Found"));

        trainingType.setTrainingType(dto.getTrainingType());
        trainingType.setIsActive(dto.getIsActive());

        TrainingType updatedType = trainingTypeRepo.save(trainingType);

        // Track update in audit trail
        auditLogService.logAudit(
                updatedType.getTrainingTypeId(),
                "UPDATE_TRAINING_TYPE",
                "TRAINING_TYPES",
                "Updated type to: " + updatedType.getTrainingType() + " (Active: " + updatedType.getIsActive() + ")"
        );
    }

    @Transactional
    public void deleteTrainingType(Long id) {
        // Fetch and log details before executing the hard deletion
        trainingTypeRepo.findById(id).ifPresent(type -> {
            auditLogService.logAudit(
                    id,
                    "DELETE_TRAINING_TYPE",
                    "TRAINING_TYPES",
                    "Deleted type: " + type.getTrainingType()
            );
        });

        trainingTypeRepo.deleteById(id);
    }

    public TrainingType getTrainingTypeById(Long id) {
        return trainingTypeRepo.findById(id).orElse(null);
    }

    public TrainingTypeDTO convertToDTO(TrainingType trainingType) {
        if (trainingType == null) {
            return null;
        }

        TrainingTypeDTO dto = new TrainingTypeDTO();
        dto.setTrainingTypeId(trainingType.getTrainingTypeId());
        dto.setTrainingType(trainingType.getTrainingType());
        dto.setIsActive(trainingType.getIsActive());

        return dto;
    }
}