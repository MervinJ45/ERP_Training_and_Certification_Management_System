package com.example.repo;
import com.example.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApprovalStatusRepo extends JpaRepository<ApprovalStatus, Long> {
    Optional<ApprovalStatus> findByApprovalStatus(String approved);
}
