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
    private final AuditLogService auditLogService; // Injected Audit Log Service

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
        // Determine action before saving while ID nullability state represents creation vs update
        boolean isNew = (user.getUserId() == null);
        String action = isNew ? "CREATE_USER" : "UPDATE_USER";

        User savedUser = userRepo.save(user);

        // Construct descriptive log information
        String roleName = savedUser.getRole() != null ? savedUser.getRole().getRoleName() : "N/A";
        String details = String.format("Username: %s, Role: %s", savedUser.getUsername(), roleName);

        // Track creation or modification in audit trail
        auditLogService.logAudit(
                savedUser.getUserId(),
                action,
                "USERS",
                details
        );

        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        // Fetch and track details before executing the hard deletion from DB
        userRepo.findById(id).ifPresent(user -> {
            auditLogService.logAudit(
                    id,
                    "DELETE_USER",
                    "USERS",
                    "Deleted user: " + user.getUsername()
            );
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
        return userRepo.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(value.toLowerCase()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        // FIX: Do not pass the entity to employeeService.convertToDTO()
        if (user.getEmployee() != null) {
            dto.setEmployeeName(user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
            // If your UserDTO absolutely needs an employee ID reference field:
            // dto.setEmployeeId(user.getEmployee().getEmployeeId());
        }

        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getRoleName());
            // Safeguard against internal nested mappings causing loops:
            dto.setRole(roleService.convertToDTO(user.getRole()));
        }

        return dto;
    }
}