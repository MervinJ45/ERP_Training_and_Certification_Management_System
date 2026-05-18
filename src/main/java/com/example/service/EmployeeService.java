package com.example.service;

import com.example.dto.EmployeeDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.repo.EmployeeRepo;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepo employeeRepo, UserService userService, PasswordEncoder passwordEncoder, DepartmentService departmentService, RoleService roleService, AuditLogService auditLogService) {

        this.employeeRepo = employeeRepo;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
        this.roleService = roleService;
        this.auditLogService = auditLogService;
    }

    public List<EmployeeDTO> getAllEmployeeDTOs() {

        return employeeRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAllManagerDTOs() {

        return employeeRepo.findAllManagers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<EmployeeDTO> searchEmployeeDTOs(String value) {

        return employeeRepo.findAll().stream().filter(employee -> employee.getFirstName().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void registerEmployee(EmployeeDTO dto) {

        Employee employee = new Employee();

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());

        employee.setDepartment(departmentService.getDepartmentById(dto.getDepartment().getDepartmentId()));

        employee.setIsActive(dto.getIsActive());

        // AUTO GENERATE EMPLOYEE CODE
        Long nextEmployeeNumber = employeeRepo.count() + 1;

        employee.setEmployeeCode("EMP" + nextEmployeeNumber);

        // SET MANAGER
        if (dto.getManager() != null) {

            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);

            employee.setManager(managerEmployee);
        }

        // SAVE EMPLOYEE FIRST
        Employee savedEmployee = employeeRepo.save(employee);

        // CREATE USER
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        user.setRole(roleService.getRoleById(dto.getRole().getRoleId()));

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // SAVE USER
        User savedUser = userService.saveUser(user);

        // LINK BOTH SIDES
        savedEmployee.setUser(savedUser);
        savedUser.setEmployee(savedEmployee);

        // SAVE AGAIN TO UPDATE RELATION
        employeeRepo.save(savedEmployee);
        userService.saveUser(savedUser);

        String fullName = savedEmployee.getFirstName() + " " + savedEmployee.getLastName();

        auditLogService.logAudit(savedEmployee.getEmployeeId(), "INSERT", "employees", "Registered new employee: " + fullName + " with account username: " + user.getUsername());
    }

    @Transactional
    public void updateEmployee(EmployeeDTO dto) {

        Employee employee = employeeRepo.findById(dto.getEmployeeId()).orElseThrow(() -> new RuntimeException("Employee Not Found"));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());

        employee.setDepartment(departmentService.getDepartmentById(dto.getDepartment().getDepartmentId()));

        employee.setIsActive(dto.getIsActive());

        // UPDATE MANAGER
        if (dto.getManager() != null) {

            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);

            employee.setManager(managerEmployee);

        } else {

            employee.setManager(null);
        }

        // UPDATE USER
        if (employee.getUser() != null) {

            employee.getUser().setEmail(dto.getEmail());

            if (dto.getRole() != null) {

                employee.getUser().setRole(roleService.getRoleById(dto.getRole().getRoleId()));
            }

            // KEEP BOTH SIDES SYNCED
            employee.getUser().setEmployee(employee);

            userService.saveUser(employee.getUser());
        }

        Employee updatedEmployee = employeeRepo.save(employee);

        String fullName = updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName();

        auditLogService.logAudit(updatedEmployee.getEmployeeId(), "UPDATE", "employees", "Updated employee details for: " + fullName);
    }

    @Transactional
    public void deleteEmployee(Long id) {

        employeeRepo.findById(id).ifPresent(employee -> {

            employeeRepo.deleteById(id);

            String fullName = employee.getFirstName() + " " + employee.getLastName();

            auditLogService.logAudit(id, "DELETE", "employees", "Deleted employee record for: " + fullName);
        });
    }

    public Employee getEmployeeById(Long id) {

        return employeeRepo.findById(id).orElse(null);
    }

    public List<EmployeeDTO> getAllTrainerDTOs() {

        return employeeRepo.findAllTrainers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<Employee> getAllManagers() {

        return employeeRepo.findAllManagers();
    }

    public EmployeeDTO convertToDTO(Employee employee) {

        if (employee == null) {
            return null;
        }

        EmployeeDTO dto = new EmployeeDTO();

        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDesignation(employee.getDesignation());
        dto.setDateOfJoining(employee.getDateOfJoining());

        dto.setDepartment(departmentService.convertToDTO(employee.getDepartment()));

        dto.setIsActive(employee.getIsActive());

        // MANAGER
        if (employee.getManager() != null) {

            EmployeeDTO managerSummary = new EmployeeDTO();

            managerSummary.setEmployeeId(employee.getManager().getEmployeeId());

            managerSummary.setFirstName(employee.getManager().getFirstName());

            managerSummary.setLastName(employee.getManager().getLastName());

            dto.setManager(managerSummary);
        }

        // USER
        if (employee.getUser() != null) {

            dto.setUsername(employee.getUser().getUsername());

            if (employee.getUser().getRole() != null) {

                dto.setRole(roleService.convertToDTO(employee.getUser().getRole()));
            }
        }

        return dto;
    }
}