package com.example.service;

import com.example.dto.TrainingTypeDTO;
import com.example.entity.TrainingType;
import com.example.repo.TrainingTypeRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepo trainingTypeRepo;

    public TrainingTypeService(TrainingTypeRepo trainingTypeRepo) {
        this.trainingTypeRepo = trainingTypeRepo;
    }

    public List<TrainingTypeDTO> getAllTrainingTypeDTOs() {

        return trainingTypeRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void saveTrainingType(TrainingTypeDTO dto) {

        TrainingType trainingType = new TrainingType();

        trainingType.setTrainingType(dto.getTrainingType());

        trainingType.setIsActive(dto.getIsActive());

        trainingTypeRepo.save(trainingType);
    }

    public void updateTrainingType(TrainingTypeDTO dto) {

        TrainingType trainingType = trainingTypeRepo.findById(dto.getTrainingTypeId()).orElseThrow(() -> new RuntimeException("Training Type Not Found"));

        trainingType.setTrainingType(dto.getTrainingType());

        trainingType.setIsActive(dto.getIsActive());

        trainingTypeRepo.save(trainingType);
    }

    public void deleteTrainingType(UUID id) {

        trainingTypeRepo.deleteById(id);
    }

    public TrainingType getTrainingTypeById(UUID id) {

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