package com.example.service;

import com.example.dto.EmployeeDTO;
import com.example.dto.TrainingCourseDTO;
import com.example.dto.UserDTO;
import com.example.entity.Employee;
import com.example.entity.TrainingCourse;
import com.example.entity.User;
import com.example.repo.TrainingCourseRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingCourseService {

    private final TrainingCourseRepo trainingCourseRepo;

    private final TrainingCategoryService trainingCategoryService;
    private final TrainingTypeService trainingTypeService;
    private final EmployeeService employeeService;
    private final UserService userService;

    public TrainingCourseService(TrainingCourseRepo trainingCourseRepo, TrainingCategoryService trainingCategoryService, TrainingTypeService trainingTypeService, EmployeeService employeeService, UserService userService) {

        this.trainingCourseRepo = trainingCourseRepo;
        this.trainingCategoryService = trainingCategoryService;
        this.trainingTypeService = trainingTypeService;
        this.employeeService = employeeService;
        this.userService = userService;
    }

    public List<TrainingCourseDTO> getAllCourseDTOs() {

        return trainingCourseRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void saveCourse(TrainingCourseDTO dto) {

        TrainingCourse course = new TrainingCourse();
        course.setCourseName(dto.getCourseName());
        course.setCategory(trainingCategoryService.getCategoryById(dto.getCategory().getCategoryId()));
        course.setDescription(dto.getDescription());
        course.setDurationDays(dto.getDurationDays());
        course.setTrainingCost(dto.getTrainingCost());

        if (dto.getTrainer() != null) {
            Employee trainer = employeeService.getEmployeeById(dto.getTrainer().getEmployeeId());
            course.setTrainer(trainer);
        }

        course.setTrainingType(trainingTypeService.getTrainingTypeById(dto.getTrainingType().getTrainingTypeId()));
        course.setCertificationProvided(dto.getCertificationProvided());
        course.setCertificationValidityMonths(dto.getCertificationValidityMonths());
        course.setMaxParticipants(dto.getMaxParticipants());
        course.setIsActive(dto.isActive());
        course.setCreatedAt(dto.getCreatedAt());

        if (dto.getCreatedBy() != null) {
            User createdBy = userService.getUserById(dto.getCreatedBy().getUserId());
            course.setCreatedBy(createdBy);
        }

        trainingCourseRepo.save(course);
    }

    public void updateCourse(TrainingCourseDTO dto) {

        TrainingCourse course = trainingCourseRepo.findById(dto.getCourseId()).orElseThrow(() -> new RuntimeException("Course Not Found"));
        course.setCourseName(dto.getCourseName());
        course.setCategory(trainingCategoryService.getCategoryById(dto.getCategory().getCategoryId()));
        course.setDescription(dto.getDescription());
        course.setDurationDays(dto.getDurationDays());
        course.setTrainingCost(dto.getTrainingCost());

        if (dto.getTrainer() != null) {
            Employee trainer = employeeService.getEmployeeById(dto.getTrainer().getEmployeeId());
            course.setTrainer(trainer);
        } else {
            course.setTrainer(null);
        }

        course.setTrainingType(trainingTypeService.getTrainingTypeById(dto.getTrainingType().getTrainingTypeId()));
        course.setCertificationProvided(dto.getCertificationProvided());
        course.setCertificationValidityMonths(dto.getCertificationValidityMonths());
        course.setMaxParticipants(dto.getMaxParticipants());
        course.setIsActive(dto.isActive());

        trainingCourseRepo.save(course);
    }

    public void deleteCourse(UUID id) {
        trainingCourseRepo.deleteById(id);
    }

    public TrainingCourse getCourseById(UUID id) {
        return trainingCourseRepo.findById(id).orElse(null);
    }

    public List<TrainingCourseDTO> searchCourseDTOs(String value) {
        return trainingCourseRepo.findAll().stream().filter(course -> course.getCourseName().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingCourseDTO> findAllCourses(String value) {
        if (value == null || value.isEmpty()) {
            return getAllCourseDTOs();
        } else {
            return searchCourseDTOs(value);
        }
    }

    public TrainingCourseDTO convertToDTO(TrainingCourse course) {

        if (course == null) {
            return null;
        }

        TrainingCourseDTO dto = new TrainingCourseDTO();

        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDurationDays(course.getDurationDays());
        dto.setTrainingCost(course.getTrainingCost());
        dto.setCertificationProvided(course.getCertificationProvided());
        dto.setCertificationValidityMonths(course.getCertificationValidityMonths());
        dto.setMaxParticipants(course.getMaxParticipants());

        if (course.getCategory() != null) {
            dto.setCategory(trainingCategoryService.convertToDTO(course.getCategory()));
            dto.setCategoryName(course.getCategory().getCategoryName());
        }

        if (course.getTrainer() != null) {
            EmployeeDTO trainerDTO = employeeService.convertToDTO(course.getTrainer());
            dto.setTrainer(trainerDTO);
            dto.setTrainerName(course.getTrainer().getFirstName() + " " + course.getTrainer().getLastName());
        }

        if (course.getTrainingType() != null) {
            dto.setTrainingType(trainingTypeService.convertToDTO(course.getTrainingType()));
            dto.setTrainingTypeName(course.getTrainingType().getTrainingType());
        }

        if (course.getCreatedBy() != null) {
            UserDTO userDTO = userService.convertToDTO(course.getCreatedBy());
            dto.setCreatedBy(userDTO);
            dto.setCreatedByName(course.getCreatedBy().getUsername());
        }

        return dto;
    }
}