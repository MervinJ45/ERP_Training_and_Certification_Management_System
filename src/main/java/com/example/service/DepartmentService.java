package com.example.service;

import com.example.dto.DepartmentDTO;
import com.example.entity.Department;
import com.example.repo.DepartmentRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepo departmentRepo;
    private final AuditLogService auditLogService;

    public DepartmentService(DepartmentRepo departmentRepo, AuditLogService auditLogService) {
        this.departmentRepo = departmentRepo;
        this.auditLogService = auditLogService;
    }

    public List<DepartmentDTO> getAllDepartmentDTOs() {
        return departmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Department saveDepartment(Department department) {
        boolean isUpdate = department.getDepartmentId() != null;

        Department savedDepartment = departmentRepo.save(department);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " department: " + savedDepartment.getDepartmentName();

        auditLogService.logAudit(savedDepartment.getDepartmentId(), action, "departments", details);

        return savedDepartment;
    }

    public void deleteDepartment(Long id) {
        departmentRepo.findById(id).ifPresent(department -> {
            departmentRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "departments", "Deleted department: " + department.getDepartmentName());
        });
    }

    public Department getDepartmentById(Long id) {
        return departmentRepo.findById(id).orElse(null);
    }

    public DepartmentDTO convertToDTO(Department department) {
        if (department == null) return null;

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setAnnualBudget(department.getAnnualBudget());
        dto.setIsActive(department.getIsActive());

        return dto;
    }
}