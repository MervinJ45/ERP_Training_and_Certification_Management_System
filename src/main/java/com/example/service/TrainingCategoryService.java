package com.example.service;

import com.example.dto.TrainingCategoryDTO;
import com.example.entity.TrainingCategory;
import com.example.repo.TrainingCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingCategoryService {

    private final TrainingCategoryRepo trainingCategoryRepo;

    public TrainingCategoryService(TrainingCategoryRepo trainingCategoryRepo) {
        this.trainingCategoryRepo = trainingCategoryRepo;
    }

    public List<TrainingCategoryDTO> getAllCategoryDTOs() {
        return trainingCategoryRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void deleteCategory(Long id) {
        trainingCategoryRepo.deleteById(id);
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