package com.example.repo;

import com.example.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Employee findAllByFirstName(String value);

    @Query("SELECT e FROM Employee e JOIN User u ON u.employee = e WHERE u.role.roleName = 'TRAINER'")
    List<Employee> findAllTrainers();

    @Query("SELECT e FROM Employee e JOIN User u ON u.employee = e WHERE u.role.roleName = 'MANAGER'")
    List<Employee> findAllManagers();

}
