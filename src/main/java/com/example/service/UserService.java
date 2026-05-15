package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.repo.UserRepo;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final AuditLogService auditLogService;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, @Lazy EmployeeService employeeService, RoleService roleService, AuditLogService auditLogService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.employeeService = employeeService;
        this.roleService = roleService;
        this.auditLogService = auditLogService;
    }

    public List<UserDTO> getAllUserDTOs() {
        return userRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public User saveUser(User user) {
        boolean isNew = (user.getUserId() == null);
        String action = isNew ? "CREATE_USER" : "UPDATE_USER";

        User savedUser = userRepo.save(user);

        String details = String.format("Username: %s, Role: %s", savedUser.getUsername(), savedUser.getRole() != null ? savedUser.getRole().getRoleName() : "N/A");

        auditLogService.logAudit(savedUser.getUserId(), action, "USERS", details);

        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepo.findById(id).ifPresent(user -> {
            // Log the deletion before the record is gone
            auditLogService.logAudit(id, "DELETE_USER", "USERS", "Deleted user: " + user.getUsername());
        });
        userRepo.deleteById(id);
    }

    public UserDTO getUserDTOById(Long id) {
        User user = userRepo.findById(id).orElse(null);
        return (user != null) ? convertToDTO(user) : null;
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String value) {
        return userRepo.findByUsername(value);
    }

    public User findByEmployeeId(Long employeeId) {
        return userRepo.findByEmployeeEmployeeId(employeeId).orElse(null);
    }

    public List<UserDTO> searchUserDTOs(String value) {
        return userRepo.findAll().stream().filter(user -> user.getUsername().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        if (user.getEmployee() != null) {
            dto.setEmployee(employeeService.convertToDTO(user.getEmployee()));
            dto.setEmployeeName(user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
        }

        if (user.getRole() != null) {
            dto.setRole(roleService.convertToDTO(user.getRole()));
            dto.setRoleName(user.getRole().getRoleName());
        }

        return dto;
    }
}