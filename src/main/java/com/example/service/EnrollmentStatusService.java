package com.example.service;

import com.example.entity.EnrollmentStatus;
import com.example.repo.EnrollmentStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentStatusService {

    private final EnrollmentStatusRepo enrollmentStatusRepo;

    public EnrollmentStatusService(EnrollmentStatusRepo enrollmentStatusRepo) {
        this.enrollmentStatusRepo = enrollmentStatusRepo;
    }

    public List<EnrollmentStatus> getAllStatuses() {
        return enrollmentStatusRepo.findAll();
    }

    public EnrollmentStatus saveStatus(EnrollmentStatus status) {
        return enrollmentStatusRepo.save(status);
    }

    public void deleteStatus(UUID id) {
        enrollmentStatusRepo.deleteById(id);
    }

    public EnrollmentStatus getStatusById(UUID id) {
        return enrollmentStatusRepo.findById(id).orElse(null);
    }
}