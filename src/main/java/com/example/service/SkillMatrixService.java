package com.example.service;

import com.example.entity.SkillMatrix;
import com.example.repo.SkillMatrixRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SkillMatrixService {

    private final SkillMatrixRepo skillMatrixRepo;

    public SkillMatrixService(SkillMatrixRepo skillMatrixRepo) {
        this.skillMatrixRepo = skillMatrixRepo;
    }

    public List<SkillMatrix> getAllSkills() {
        return skillMatrixRepo.findAll();
    }

    public SkillMatrix saveSkill(SkillMatrix skill) {
        return skillMatrixRepo.save(skill);
    }

    public void deleteSkill(Long id) {
        skillMatrixRepo.deleteById(id);
    }

    public SkillMatrix getSkillById(Long id) {
        return skillMatrixRepo.findById(id).orElse(null);
    }
}