package com.example.service;

import com.example.dto.SkillSummaryDTO;
import com.example.entity.Employee;
import com.example.entity.SkillMatrix;
import com.example.entity.TrainingCourse;
import com.example.repo.SkillMatrixRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillMatrixService {

    private static final Logger logger = LoggerFactory.getLogger(SkillMatrixService.class);

    private final SkillMatrixRepo skillMatrixRepo;
    private final AuditLogService auditLogService;

    public SkillMatrixService(SkillMatrixRepo skillMatrixRepo, AuditLogService auditLogService) {
        this.skillMatrixRepo = skillMatrixRepo;
        this.auditLogService = auditLogService;
    }

    public List<SkillMatrix> getAllSkills() {

        logger.info("Fetching all skill matrix records");

        return skillMatrixRepo.findAll();
    }

    public SkillMatrix saveSkill(SkillMatrix skill) {

        boolean isUpdate = skill.getSkillId() != null;

        logger.info("{} operation started for skill matrix", isUpdate ? "UPDATE" : "CREATE");

        SkillMatrix savedSkill = skillMatrixRepo.save(skill);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " skill matrix entry.";

        auditLogService.logAudit(savedSkill.getSkillId(), action, "skill_matrix", details);

        logger.info("Skill matrix saved successfully with id: {}", savedSkill.getSkillId());

        return savedSkill;
    }

    public void deleteSkill(Long id) {

        logger.info("Deleting skill matrix id: {}", id);

        skillMatrixRepo.findById(id).ifPresent(skill -> {

            skillMatrixRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "skill_matrix", "Deleted skill matrix record ID: " + id);

            logger.info("Skill matrix deleted successfully with id: {}", id);
        });
    }

    public SkillMatrix getSkillById(Long id) {

        logger.info("Fetching skill matrix by id: {}", id);

        return skillMatrixRepo.findById(id).orElse(null);
    }

    @Transactional
    public void createSkillEntry(Employee student, TrainingCourse course, Integer rating, LocalDateTime now) {

        logger.info("Creating skill entry for employee: {} {}", student.getFirstName(), student.getLastName());

        SkillMatrix skillMatrix = new SkillMatrix();

        skillMatrix.setEmployee(student);
        skillMatrix.setCourse(course);
        skillMatrix.setCreatedAt(now);
        skillMatrix.setSkillName(course.getCourseName());
        skillMatrix.setProficiencyRating(rating);
        skillMatrix.setUpdatedAt(now);
        skillMatrix.setIsActive(true);

        SkillMatrix savedSkill = skillMatrixRepo.save(skillMatrix);

        auditLogService.logAudit(savedSkill.getSkillId(), "INSERT", "skill_matrix", "Created skill entry for employee: " + student.getFirstName() + " " + student.getLastName() + ", Skill: " + savedSkill.getSkillName());

        logger.info("Skill entry created successfully with id: {}", savedSkill.getSkillId());
    }

    public List<SkillSummaryDTO> getSkillMatrixSummary() {

        logger.info("Fetching skill matrix summary");

        return skillMatrixRepo.findByIsActiveTrue().stream().map(matrix -> {

            String fullName = "";

            if (matrix.getEmployee() != null) {
                fullName = matrix.getEmployee().getFirstName() + " " + matrix.getEmployee().getLastName();
            }

            String courseTitle = matrix.getCourse() != null ? matrix.getCourse().getCourseName() : "N/A";

            return new SkillSummaryDTO(matrix.getSkillId(), fullName, courseTitle, matrix.getSkillName(), matrix.getProficiencyRating(), matrix.getUpdatedAt());

        }).collect(Collectors.toList());
    }

    public List<SkillMatrix> getSkillsByEmployeeId(Long employeeId) {
        return skillMatrixRepo.findByEmployee_EmployeeId(employeeId);
    }
}