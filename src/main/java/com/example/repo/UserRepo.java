    package com.example.repo;

    import com.example.entity.Employee;
    import com.example.entity.User;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;
    import java.util.Optional;

    import java.util.UUID;

    public interface UserRepo  extends JpaRepository<User, Long> {

        Optional<User> findByUsername(String username);

        Optional<User> findByEmployeeEmployeeId(Long employeeId);

        boolean existsByUsername(String superAdmin);
    }
