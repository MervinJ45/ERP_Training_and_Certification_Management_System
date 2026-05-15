package com.example.service;

import com.example.dto.TrainingCourseDTO;
import com.example.entity.AuditLog;
import com.example.entity.TrainingCourse;
import com.example.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingCourseService {

    private final TrainingCourseRepo trainingCourseRepo;
    private final TrainingCategoryRepo categoryRepo;
    private final TrainingTypeRepo trainingTypeRepo;
    private final EmployeeRepo employeeRepo;
    private final UserRepo userRepo;
    private final AuditLogRepo auditLogRepo;

    private final UserService userService;
    private final TrainingCategoryService categoryService;
    private final TrainingTypeService trainingTypeService;
    private final EmployeeService employeeService;

    public TrainingCourseService(TrainingCourseRepo trainingCourseRepo, TrainingCategoryRepo categoryRepo, TrainingTypeRepo trainingTypeRepo, EmployeeRepo employeeRepo, UserRepo userRepo, AuditLogRepo auditLogRepo, UserService userService, TrainingCategoryService categoryService, TrainingTypeService trainingTypeService, EmployeeService employeeService) {
        this.trainingCourseRepo = trainingCourseRepo;
        this.categoryRepo = categoryRepo;
        this.trainingTypeRepo = trainingTypeRepo;
        this.employeeRepo = employeeRepo;
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
        this.userService = userService;
        this.categoryService = categoryService;
        this.trainingTypeService = trainingTypeService;
        this.employeeService = employeeService;
    }

    public List<TrainingCourseDTO> getAllCourseDTOs() {
        return trainingCourseRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingCourseDTO getCourseDTOById(Long id) {
        return trainingCourseRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    @Transactional
    public TrainingCourseDTO saveCourse(TrainingCourseDTO dto, Long currentUserId, Long currentRoleId) {
        TrainingCourse course;
        boolean isUpdate = dto.getCourseId() != null;

        if (isUpdate) {
            course = trainingCourseRepo.findById(dto.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));
        } else {
            course = new TrainingCourse();
            // Assign creator on first save
            if (dto.getCreatedBy() != null && dto.getCreatedBy().getUserId() != null) {
                course.setCreator(userRepo.findById(dto.getCreatedBy().getUserId()).orElse(null));
            }
        }

        // Basic Field Mapping
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setDurationDays(dto.getDurationDays());
        course.setTrainingCost(dto.getTrainingCost());
        course.setCertificationProvided(dto.getCertificationProvided());
        course.setCertificationValidityMonths(dto.getCertificationValidityMonths());
        course.setMaxParticipants(dto.getMaxParticipants());
        course.setIsActive(dto.isActive());

        // Relationship Mapping
        if (dto.getCategory() != null) {
            course.setCategory(categoryRepo.findById(dto.getCategory().getCategoryId()).orElse(null));
        }
        if (dto.getTrainer() != null) {
            course.setTrainer(employeeRepo.findById(dto.getTrainer().getEmployeeId()).orElse(null));
        }
        if (dto.getTrainingType() != null) {
            course.setTrainingType(trainingTypeRepo.findById(dto.getTrainingType().getTrainingTypeId()).orElse(null));
        }

        TrainingCourse saved = trainingCourseRepo.save(course);

        logAudit(currentUserId, currentRoleId, saved.getCourseId(), isUpdate ? "UPDATE" : "INSERT", "training_courses", "Course: " + saved.getCourseName());

        return convertToDTO(saved);
    }

    @Transactional
    public TrainingCourseDTO updateCourse(TrainingCourseDTO dto) {

        if (dto.getCourseId() == null) {
            throw new RuntimeException("Course ID is required for update");
        }

        TrainingCourse course = trainingCourseRepo.findById(dto.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));

        // Basic Field Mapping
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setDurationDays(dto.getDurationDays());
        course.setTrainingCost(dto.getTrainingCost());
        course.setCertificationProvided(dto.getCertificationProvided());
        course.setCertificationValidityMonths(dto.getCertificationValidityMonths());
        course.setMaxParticipants(dto.getMaxParticipants());
        course.setIsActive(dto.isActive());

        // Relationship Mapping
        if (dto.getCategory() != null) {
            course.setCategory(categoryRepo.findById(dto.getCategory().getCategoryId()).orElse(null));
        }

        if (dto.getTrainer() != null) {
            course.setTrainer(employeeRepo.findById(dto.getTrainer().getEmployeeId()).orElse(null));
        }

        if (dto.getTrainingType() != null) {
            course.setTrainingType(trainingTypeRepo.findById(dto.getTrainingType().getTrainingTypeId()).orElse(null));
        }

        TrainingCourse updatedCourse = trainingCourseRepo.save(course);

        return convertToDTO(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id, Long userId, Long roleId) {
        trainingCourseRepo.findById(id).ifPresent(course -> {
            course.setIsActive(false);
            trainingCourseRepo.save(course);
            logAudit(userId, roleId, id, "DEACTIVATE", "training_courses", "Marked course as inactive");
        });
    }

    public TrainingCourseDTO convertToDTO(TrainingCourse course) {
        if (course == null) return null;

        TrainingCourseDTO dto = new TrainingCourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setCourseDescription(course.getDescription()); // Mapping both to be safe
        dto.setDurationDays(course.getDurationDays());
        dto.setTrainingCost(course.getTrainingCost());
        dto.setCertificationProvided(course.getCertificationProvided());
        dto.setCertificationValidityMonths(course.getCertificationValidityMonths());
        dto.setMaxParticipants(course.getMaxParticipants());
        dto.setActive(course.getIsActive() != null ? course.getIsActive() : false);
        dto.setCreatedAt(course.getCreatedAt());

        // Mapping Relationships & Flattening names
        if (course.getCategory() != null) {
            dto.setCategory(categoryService.convertToDTO(course.getCategory()));
            dto.setCategoryName(course.getCategory().getCategoryName());
        }

        if (course.getTrainer() != null) {
            dto.setTrainer(employeeService.convertToDTO(course.getTrainer()));
            dto.setTrainerName(course.getTrainer().getFirstName() + " " + course.getTrainer().getLastName());
        }

        if (course.getTrainingType() != null) {
            dto.setTrainingType(trainingTypeService.convertToDTO(course.getTrainingType()));
            dto.setTrainingTypeName(course.getTrainingType().getTrainingType());
        }

        // Entity "creator" maps to DTO "createdBy"
        if (course.getCreator() != null) {
            dto.setCreatedBy(userService.convertToDTO(course.getCreator()));
            dto.setCreatedByName(course.getCreator().getUsername());
        }

        return dto;
    }

    private void logAudit(Long userId, Long roleId, Long recordId, String action, String table, String details) {
        AuditLog log = new AuditLog();
        log.setRecordId(recordId);
        log.setAction(action);
        log.setTableAffected(table);
        log.setChangeDetails(details);
        log.setActionTime(LocalDateTime.now());
        auditLogRepo.save(log);
    }

    public List<TrainingCourseDTO> searchCourseDTOs(String value) {
        return trainingCourseRepo.findAll().stream().filter(course -> course.getCourseName() != null && course.getCourseName().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }
}