package com.example.service;

import com.example.dto.TrainingTypeDTO;
import com.example.entity.TrainingType;
import com.example.repo.TrainingTypeRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingTypeService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeService.class);

    private final TrainingTypeRepo trainingTypeRepo;
    private final AuditLogService auditLogService;

    public TrainingTypeService(TrainingTypeRepo trainingTypeRepo, AuditLogService auditLogService) {
        this.trainingTypeRepo = trainingTypeRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingTypeDTO> getAllTrainingTypeDTOs() {
        logger.info("Fetching all training types");

        return trainingTypeRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void saveTrainingType(TrainingTypeDTO dto) {
        logger.info("Creating training type: {}", dto.getTrainingType());

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingType(dto.getTrainingType());
        trainingType.setIsActive(dto.getIsActive());

        TrainingType savedType = trainingTypeRepo.save(trainingType);

        auditLogService.logAudit(savedType.getTrainingTypeId(), "CREATE_TRAINING_TYPE", "TRAINING_TYPES", "Created type: " + savedType.getTrainingType());

        logger.info("Training type created successfully: {}", savedType.getTrainingType());
    }

    @Transactional
    public void updateTrainingType(TrainingTypeDTO dto) {
        logger.info("Updating training type id: {}", dto.getTrainingTypeId());

        TrainingType trainingType = trainingTypeRepo.findById(dto.getTrainingTypeId()).orElseThrow(() -> new RuntimeException("Training Type Not Found"));

        trainingType.setTrainingType(dto.getTrainingType());
        trainingType.setIsActive(dto.getIsActive());

        TrainingType updatedType = trainingTypeRepo.save(trainingType);

        auditLogService.logAudit(updatedType.getTrainingTypeId(), "UPDATE_TRAINING_TYPE", "TRAINING_TYPES", "Updated type to: " + updatedType.getTrainingType() + " (Active: " + updatedType.getIsActive() + ")");

        logger.info("Training type updated successfully: {}", updatedType.getTrainingType());
    }

    @Transactional
    public void deleteTrainingType(Long id) {
        logger.info("Deleting training type id: {}", id);

        trainingTypeRepo.findById(id).ifPresent(type -> {
            auditLogService.logAudit(id, "DELETE_TRAINING_TYPE", "TRAINING_TYPES", "Deleted type: " + type.getTrainingType());

            logger.info("Training type deleted: {}", type.getTrainingType());
        });

        trainingTypeRepo.deleteById(id);
    }

    public TrainingType getTrainingTypeById(Long id) {
        logger.info("Fetching training type by id: {}", id);

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