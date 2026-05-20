package com.example.service;

import com.example.dto.RoleDTO;
import com.example.entity.Role;
import com.example.repo.RoleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepo roleRepo;
    private final AuditLogService auditLogService;

    public RoleService(RoleRepo roleRepo, AuditLogService auditLogService) {
        this.roleRepo = roleRepo;
        this.auditLogService = auditLogService;
    }

    public List<RoleDTO> getAllRoleDTOs() {

        logger.info("Fetching all roles");

        return roleRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Role saveRole(Role role) {

        boolean isUpdate = role.getRoleId() != null;

        logger.info("{} operation started for role: {}", isUpdate ? "UPDATE" : "CREATE", role.getRoleName());

        Role savedRole = roleRepo.save(role);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " user role: " + savedRole.getRoleName();

        auditLogService.logAudit(savedRole.getRoleId(), action, "ROLES", details);

        logger.info("Role saved successfully with id: {}", savedRole.getRoleId());

        return savedRole;
    }

    public void deleteRole(Long id) {

        logger.info("Deleting role id: {}", id);

        roleRepo.findById(id).ifPresent(role -> {

            roleRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "ROLES", "Deleted user role: " + role.getRoleName());

            logger.info("Role deleted successfully: {}", role.getRoleName());
        });
    }

    public Role getRoleById(Long id) {

        logger.info("Fetching role by id: {}", id);

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