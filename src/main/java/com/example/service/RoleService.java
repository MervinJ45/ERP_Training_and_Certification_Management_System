package com.example.service;

import com.example.dto.RoleDTO;
import com.example.entity.Role;
import com.example.repo.RoleRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepo roleRepo;
    private final AuditLogService auditLogService;

    public RoleService(RoleRepo roleRepo, AuditLogService auditLogService) {
        this.roleRepo = roleRepo;
        this.auditLogService = auditLogService;
    }

    public List<RoleDTO> getAllRoleDTOs() {
        return roleRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Role saveRole(Role role) {
        boolean isUpdate = role.getRoleId() != null;

        Role savedRole = roleRepo.save(role);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " user role: " + savedRole.getRoleName();
        auditLogService.logAudit(savedRole.getRoleId(), action, "roles", details);
        return savedRole;
    }

    public void deleteRole(Long id) {
        roleRepo.findById(id).ifPresent(role -> {
            roleRepo.deleteById(id);
            auditLogService.logAudit(id, "DELETE", "roles", "Deleted user role: " + role.getRoleName());
        });
    }

    public Role getRoleById(Long id) {
        return roleRepo.findById(id).orElse(null);
    }

    public RoleDTO convertToDTO(Role role) {
        if (role == null) return null;

        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setIsActive(role.getIsActive());

        return dto;
    }
}