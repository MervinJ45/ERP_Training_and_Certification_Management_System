package com.example.repo;

import com.example.entity.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingCategoryRepo  extends JpaRepository<TrainingCategory, Long> {

}
