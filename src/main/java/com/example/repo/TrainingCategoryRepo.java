package com.example.repo;

import com.example.entity.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TrainingCategoryRepo  extends JpaRepository<TrainingCategory, Long> {

}
