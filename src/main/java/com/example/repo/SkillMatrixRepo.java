package com.example.repo;

import com.example.entity.SkillMatrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SkillMatrixRepo extends JpaRepository<SkillMatrix, Long> {
    
}
