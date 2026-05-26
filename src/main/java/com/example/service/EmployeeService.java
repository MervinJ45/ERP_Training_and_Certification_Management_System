package com.example.service;

import com.example.dto.EmployeeDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.repo.EmployeeRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

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

        logger.info("Fetching all employees");

        return employeeRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAllManagerDTOs() {

        logger.info("Fetching all managers");

        return employeeRepo.findAllManagers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<EmployeeDTO> searchEmployeeDTOs(String value) {

        logger.info("Searching employees with keyword: {}", value);

        return employeeRepo.findAll().stream().filter(employee -> employee.getFirstName().toLowerCase().contains(value.toLowerCase())).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void registerEmployee(EmployeeDTO dto) {

        logger.info("Registering new employee: {} {}", dto.getFirstName(), dto.getLastName());

        Employee employee = new Employee();

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());

        employee.setDepartment(departmentService.getDepartmentById(dto.getDepartment().getDepartmentId()));

        employee.setIsActive(dto.getIsActive());

        Long nextEmployeeNumber = employeeRepo.count() + 1;

        employee.setEmployeeCode("EMP" + nextEmployeeNumber);

        if (dto.getManager() != null) {

            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);

            employee.setManager(managerEmployee);
        }

        Employee savedEmployee = employeeRepo.save(employee);

        User user = new User();

        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        user.setRole(roleService.getRoleById(dto.getRole().getRoleId()));

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userService.saveUser(user);

        savedEmployee.setUser(savedUser);
        savedUser.setEmployee(savedEmployee);

        employeeRepo.save(savedEmployee);
        userService.saveUser(savedUser);

        String fullName = savedEmployee.getFirstName() + " " + savedEmployee.getLastName();

        auditLogService.logAudit(savedEmployee.getEmployeeId(), "INSERT", "employees", "Registered new employee: " + fullName + " with account username: " + user.getUsername());

        logger.info("Employee registered successfully with id: {}", savedEmployee.getEmployeeId());
    }

    @Transactional
    public void updateEmployee(EmployeeDTO dto) {

        logger.info("Updating employee with id: {}", dto.getEmployeeId());

        Employee employee = employeeRepo.findById(dto.getEmployeeId()).orElseThrow(() -> new RuntimeException("Employee Not Found"));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());

        employee.setDepartment(departmentService.getDepartmentById(dto.getDepartment().getDepartmentId()));

        employee.setIsActive(dto.getIsActive());

        if (dto.getManager() != null) {

            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);

            employee.setManager(managerEmployee);

        } else {

            employee.setManager(null);
        }

        if (employee.getUser() != null) {

            employee.getUser().setEmail(dto.getEmail());

            if (dto.getRole() != null) {

                employee.getUser().setRole(roleService.getRoleById(dto.getRole().getRoleId()));
            }

            employee.getUser().setEmployee(employee);

            userService.saveUser(employee.getUser());
        }

        Employee updatedEmployee = employeeRepo.save(employee);

        String fullName = updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName();

        auditLogService.logAudit(updatedEmployee.getEmployeeId(), "UPDATE", "employees", "Updated employee details for: " + fullName);

        logger.info("Employee updated successfully with id: {}", updatedEmployee.getEmployeeId());
    }

    @Transactional
    public void deleteEmployee(Long id) {

        logger.info("Deleting employee with id: {}", id);

        employeeRepo.findById(id).ifPresent(employee -> {

            employeeRepo.deleteById(id);

            String fullName = employee.getFirstName() + " " + employee.getLastName();

            auditLogService.logAudit(id, "DELETE", "employees", "Deleted employee record for: " + fullName);

            logger.info("Employee deleted successfully: {}", fullName);
        });
    }

    public Employee getEmployeeById(Long id) {

        logger.info("Fetching employee by id: {}", id);

        return employeeRepo.findById(id).orElse(null);
    }

    public List<EmployeeDTO> getAllTrainerDTOs() {

        logger.info("Fetching all trainers");

        return employeeRepo.findAllTrainers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<Employee> getAllManagers() {

        logger.info("Fetching all manager entities");

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

        if (employee.getManager() != null) {

            EmployeeDTO managerSummary = new EmployeeDTO();

            managerSummary.setEmployeeId(employee.getManager().getEmployeeId());
            managerSummary.setFirstName(employee.getManager().getFirstName());
            managerSummary.setLastName(employee.getManager().getLastName());

            dto.setManager(managerSummary);
        }

        if (employee.getUser() != null) {

            dto.setUsername(employee.getUser().getUsername());

            if (employee.getUser().getRole() != null) {

                dto.setRole(roleService.convertToDTO(employee.getUser().getRole()));
            }
        }

        return dto;
    }
}