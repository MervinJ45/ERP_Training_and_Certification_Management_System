package com.example.service;

import com.example.dto.RoleDTO;
import com.example.entity.Role;
import com.example.repo.RoleRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepo roleRepo;

    public RoleService(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    public List<RoleDTO> getAllRoleDTOs() {
        return roleRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Role saveRole(Role role) {
        return roleRepo.save(role);
    }

    public void deleteRole(Long id) {
        roleRepo.deleteById(id);
    }

    public Role getRoleById(Long id) {
        return roleRepo.findById(id).orElse(null);
    }

    public RoleDTO convertToDTO(Role role) {

        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setIsActive(role.getIsActive());

        return dto;
    }
}