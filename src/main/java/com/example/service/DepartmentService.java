package com.example.service;

import com.example.dto.DepartmentDTO;
import com.example.entity.Department;
import com.example.repo.DepartmentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepo departmentRepo;
    private final AuditLogService auditLogService;

    public DepartmentService(DepartmentRepo departmentRepo, AuditLogService auditLogService) {
        this.departmentRepo = departmentRepo;
        this.auditLogService = auditLogService;
    }

    public List<DepartmentDTO> getAllDepartmentDTOs() {
        logger.info("Fetching all departments");
        return departmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Department saveDepartment(Department department) {
        boolean isUpdate = department.getDepartmentId() != null;
        logger.info("{} operation started for department: {}", isUpdate ? "UPDATE" : "CREATE", department.getDepartmentName());

        Department savedDepartment = departmentRepo.save(department);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " department: " + savedDepartment.getDepartmentName();
        auditLogService.logAudit(savedDepartment.getDepartmentId(), action, "departments", details);

        logger.info("Department saved successfully with id: {}", savedDepartment.getDepartmentId());
        return savedDepartment;
    }

    public void deleteDepartment(Long id) {
        logger.info("Deleting department with id: {}", id);
        departmentRepo.findById(id).ifPresent(department -> {
            departmentRepo.deleteById(id);
            auditLogService.logAudit(id, "DELETE", "departments", "Deleted department: " + department.getDepartmentName());
            logger.info("Department deleted successfully: {}", department.getDepartmentName());
        });
    }

    public Department getDepartmentById(Long id) {
        logger.info("Fetching department by id: {}", id);
        return departmentRepo.findById(id).orElse(null);
    }

    public DepartmentDTO convertToDTO(Department department) {
        if (department == null) return null;

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setAnnualBudget(department.getAnnualBudget());
        dto.setAvailableBalance(department.getAvailableBalance());
        dto.setIsActive(department.getIsActive());

        return dto;
    }
}