package com.example.service;

import com.example.dto.DepartmentDTO;
import com.example.entity.Department;
import com.example.repo.DepartmentRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepo departmentRepo;

    public DepartmentService(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    public List<DepartmentDTO> getAllDepartmentDTOs() {
        return departmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Department saveDepartment(Department department) {
        return departmentRepo.save(department);
    }

    public void deleteDepartment(Long id) {
        departmentRepo.deleteById(id);
    }

    public Department getDepartmentById(Long id) {
        return departmentRepo.findById(id).orElse(null);
    }

    public DepartmentDTO convertToDTO(Department department) {

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setAnnualBudget(department.getAnnualBudget());
        dto.setIsActive(department.getIsActive());

        return dto;
    }
}