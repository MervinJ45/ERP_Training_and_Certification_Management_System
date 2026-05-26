package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
        logger.info("Fetching all users");
        return userRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public User saveUser(User user) {
        boolean isNew = (user.getUserId() == null);
        String action = isNew ? "INSERT" : "UPDATE";

        logger.info("{} operation started for user: {}", action, user.getUsername());

        User savedUser = userRepo.save(user);

        String roleName = savedUser.getRole() != null ? savedUser.getRole().getRoleName() : "N/A";
        String details = String.format("Username: %s, Role: %s", savedUser.getUsername(), roleName);

        auditLogService.logAudit(savedUser.getUserId(), action, "users", details);

        logger.info("{} operation completed for user: {}", action, savedUser.getUsername());

        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Delete operation started for user id: {}", id);

        userRepo.findById(id).ifPresent(user -> {
            auditLogService.logAudit(id, "DELETE", "users", "Deleted user: " + user.getUsername());

            logger.info("User deleted: {}", user.getUsername());
        });

        userRepo.deleteById(id);
    }

    public UserDTO getUserDTOById(Long id) {
        logger.info("Fetching user by id: {}", id);

        User user = userRepo.findById(id).orElse(null);
        return (user != null) ? convertToDTO(user) : null;
    }

    public User getUserById(Long id) {
        logger.info("Fetching user entity by id: {}", id);

        return userRepo.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String value) {
        logger.info("Finding user by username: {}", value);

        return userRepo.findByUsername(value);
    }

    public User findByEmployeeId(Long employeeId) {
        logger.info("Finding user by employee id: {}", employeeId);

        return userRepo.findByEmployeeEmployeeId(employeeId).orElse(null);
    }

    public List<UserDTO> searchUserDTOs(String value) {
        logger.info("Searching users with value: {}", value);

        return userRepo.findAll().stream().filter(user -> user.getUsername().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public boolean updateLoggedInPassword(Long userId, String currentRawPassword, String newRawPassword) {
        logger.info("Attempting password modification for logged-in user ID: {}", userId);

        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User context not found for ID: " + userId));

        if (!passwordEncoder.matches(currentRawPassword, user.getPassword())) {
            logger.warn("Password change rejected: Incorrect current password provided for user ID: {}", userId);
            return false;
        }

        String encryptedPassword = passwordEncoder.encode(newRawPassword);
        user.setPassword(encryptedPassword);
        userRepo.save(user);

        auditLogService.logAudit(user.getUserId(), "UPDATE", "users", "User successfully modified account session password.");
        logger.info("Password successfully updated for user ID: {}", userId);
        return true;
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        if (user.getEmployee() != null) {
            dto.setEmployeeName(user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
        }

        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getRoleName());
            dto.setRole(roleService.convertToDTO(user.getRole()));
        }

        return dto;
    }
}