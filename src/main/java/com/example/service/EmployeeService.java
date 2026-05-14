package com.example.service;

import com.example.dto.EmployeeDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.repo.EmployeeRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final RoleService roleService;

    public EmployeeService(EmployeeRepo employeeRepo, UserService userService, PasswordEncoder passwordEncoder, DepartmentService departmentService, RoleService roleService) {
        this.employeeRepo = employeeRepo;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
        this.roleService = roleService;
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

        if (dto.getManager() != null) {
            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);
            employee.setManager(managerEmployee);
        }

        Employee savedEmployee = employeeRepo.save(employee);

        System.out.println(dto.getUsername());
        System.out.println(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(roleService.getRoleById(dto.getRole().getRoleId()));
        user.setEmployee(savedEmployee);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        userService.saveUser(user);
    }


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

        if (dto.getManager() != null) {

            Employee managerEmployee = employeeRepo.findById(dto.getManager().getEmployeeId()).orElse(null);
            employee.setManager(managerEmployee);

        } else {
            employee.setManager(null);
        }

        Employee updatedEmployee = employeeRepo.save(employee);

        User user = updatedEmployee.getUser();

        if (user != null) {
            user.setEmail(dto.getEmail());
            if (dto.getRole() != null) {
                user.setRole(roleService.getRoleById(dto.getRole().getRoleId()));
            }
            userService.saveUser(user);
        }
    }


    public void deleteEmployee(UUID id) {
        employeeRepo.deleteById(id);
    }

    public Employee getEmployeeById(UUID id) {
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

        if (employee.getManager() != null) {
            EmployeeDTO managerSummary = new EmployeeDTO();
            managerSummary.setEmployeeId(employee.getManager().getEmployeeId());
            managerSummary.setFirstName(employee.getManager().getFirstName());
            managerSummary.setLastName(employee.getManager().getLastName());
            dto.setManager(managerSummary);
        }

        if (employee.getUser() != null) {
            dto.setUsername(employee.getUser().getUsername());
            dto.setRole(roleService.convertToDTO(employee.getUser().getRole()));
        }

        return dto;
    }
}