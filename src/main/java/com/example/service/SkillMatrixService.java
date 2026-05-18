package com.example.service;

import com.example.dto.SkillSummaryDTO;
import com.example.entity.SkillMatrix;
import com.example.repo.SkillMatrixRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillMatrixService {

    private final SkillMatrixRepo skillMatrixRepo;
    private final AuditLogService auditLogService;

    public SkillMatrixService(SkillMatrixRepo skillMatrixRepo, AuditLogService auditLogService) {
        this.skillMatrixRepo = skillMatrixRepo;
        this.auditLogService = auditLogService;
    }

    public List<SkillMatrix> getAllSkills() {
        return skillMatrixRepo.findAll();
    }

    public SkillMatrix saveSkill(SkillMatrix skill) {
        boolean isUpdate = skill.getSkillId() != null;

        SkillMatrix savedSkill = skillMatrixRepo.save(skill);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " skill matrix entry.";

        auditLogService.logAudit(savedSkill.getSkillId(), action, "skill_matrix", details);

        return savedSkill;
    }

    public void deleteSkill(Long id) {
        skillMatrixRepo.findById(id).ifPresent(skill -> {
            skillMatrixRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "skill_matrix", "Deleted skill matrix record ID: " + id);
        });
    }

    public SkillMatrix getSkillById(Long id) {
        return skillMatrixRepo.findById(id).orElse(null);
    }

    public List<SkillSummaryDTO> getSkillMatrixSummary() {
        return skillMatrixRepo.findByIsActiveTrue().stream().map(matrix -> {
            String fullName = "Unknown Employee";
            if (matrix.getEmployee() != null) {
                fullName = matrix.getEmployee().getFirstName() + " " + matrix.getEmployee().getLastName();
            }

            String courseTitle = matrix.getCourse() != null ? matrix.getCourse().getCourseName() : "N/A";

            return new SkillSummaryDTO(matrix.getSkillId(), fullName, courseTitle, matrix.getSkillName(), matrix.getProficiencyRating(), matrix.getUpdatedAt());
        }).collect(Collectors.toList());
    }

}