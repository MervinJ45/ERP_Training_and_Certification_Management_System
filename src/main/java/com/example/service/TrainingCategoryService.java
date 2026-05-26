package com.example.service;

import com.example.dto.TrainingCategoryDTO;
import com.example.entity.TrainingCategory;
import com.example.repo.TrainingCategoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingCategoryService.class);

    private final TrainingCategoryRepo trainingCategoryRepo;
    private final AuditLogService auditLogService;

    public TrainingCategoryService(TrainingCategoryRepo trainingCategoryRepo, AuditLogService auditLogService) {
        this.trainingCategoryRepo = trainingCategoryRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingCategoryDTO> getAllCategoryDTOs() {

        logger.info("Fetching all training categories");

        return trainingCategoryRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingCategory saveCategory(TrainingCategory category) {

        boolean isUpdate = category.getCategoryId() != null;

        logger.info("{} operation started for training category: {}", isUpdate ? "UPDATE" : "CREATE", category.getCategoryName());

        TrainingCategory savedCategory = trainingCategoryRepo.save(category);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " training category: " + savedCategory.getCategoryName();

        auditLogService.logAudit(savedCategory.getCategoryId(), action, "training_categories", details);

        logger.info("Training category saved successfully with id: {}", savedCategory.getCategoryId());

        return savedCategory;
    }

    public void deleteCategory(Long id) {

        logger.info("Deleting training category id: {}", id);

        trainingCategoryRepo.findById(id).ifPresent(category -> {

            trainingCategoryRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "training_categories", "Deleted training category: " + category.getCategoryName());

            logger.info("Training category deleted successfully: {}", category.getCategoryName());
        });
    }

    public TrainingCategory getCategoryById(Long id) {

        logger.info("Fetching training category by id: {}", id);

        return trainingCategoryRepo.findById(id).orElse(null);
    }

    public TrainingCategoryDTO convertToDTO(TrainingCategory category) {
        if (category == null) {
            return null;
        }

        TrainingCategoryDTO dto = new TrainingCategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setIsActive(category.getIsActive());

        return dto;
    }
}