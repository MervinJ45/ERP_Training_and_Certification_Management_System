package com.example.service;

import com.example.dto.TrainingCategoryDTO;
import com.example.entity.TrainingCategory;
import com.example.repo.TrainingCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingCategoryService {

    private final TrainingCategoryRepo trainingCategoryRepo;
    // Injected central AuditLogService
    private final AuditLogService auditLogService;

    public TrainingCategoryService(TrainingCategoryRepo trainingCategoryRepo, AuditLogService auditLogService) {
        this.trainingCategoryRepo = trainingCategoryRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingCategoryDTO> getAllCategoryDTOs() {
        return trainingCategoryRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Added this method to support fully-audited creates/updates
    public TrainingCategory saveCategory(TrainingCategory category) {
        boolean isUpdate = category.getCategoryId() != null;

        TrainingCategory savedCategory = trainingCategoryRepo.save(category);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " training category: " + savedCategory.getCategoryName();

        auditLogService.logAudit(
                savedCategory.getCategoryId(),
                action,
                "training_categories",
                details
        );

        return savedCategory;
    }

    public void deleteCategory(Long id) {
        // Retrieve record first to preserve descriptive metadata inside the audit trail before deletion
        trainingCategoryRepo.findById(id).ifPresent(category -> {
            trainingCategoryRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "training_categories",
                    "Deleted training category: " + category.getCategoryName()
            );
        });
    }

    public TrainingCategory getCategoryById(Long id) {
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