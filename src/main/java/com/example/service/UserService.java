package com.example.service;

import com.example.dto.EmployeeDTO;
import com.example.dto.RoleDTO;
import com.example.dto.UserDTO;
import com.example.entity.Employee;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repo.EmployeeRepo;
import com.example.repo.UserRepo;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;
    private final RoleService roleService;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, @Lazy EmployeeService employeeService, RoleService roleService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.employeeService = employeeService;
        this.roleService = roleService;
    }

    public List<UserDTO> getAllUserDTOs() {
        return userRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public void deleteUser(UUID id) {
        userRepo.deleteById(id);
    }

    public UserDTO getUserDTOById(UUID id) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            return convertToDTO(user);
        }
        return null;
    }

    public User getUserById(UUID id) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            return user;
        }
        return null;
    }

    public Optional<User> findByUsername(String value) {
        return userRepo.findByUsername(value);
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

    public User findByEmployeeId(UUID employeeId) {
        return userRepo.findByEmployeeEmployeeId(employeeId).orElse(null);
    }
}