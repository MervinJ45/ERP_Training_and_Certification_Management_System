package com.example.repo;

import com.example.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingTypeRepo extends JpaRepository<TrainingType, Long> {

}
