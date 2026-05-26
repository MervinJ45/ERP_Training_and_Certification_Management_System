package com.example.dto;

import com.example.entity.Department;
import com.example.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String designation;
    private LocalDate dateOfJoining;
    private DepartmentDTO department;
    private EmployeeDTO manager;
    private RoleDTO role;
    private UserDTO user;
    private String username;
    private String password;

}