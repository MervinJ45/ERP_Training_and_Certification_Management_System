package com.example.service;

import com.example.entity.CertificationStatus;
import com.example.repo.CertificationStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificationStatusService {

    private final CertificationStatusRepo
            certificationStatusRepo;

    public CertificationStatusService(
            CertificationStatusRepo certificationStatusRepo
    ) {
        this.certificationStatusRepo =
                certificationStatusRepo;
    }

    public List<CertificationStatus> getAllStatuses() {
        return certificationStatusRepo.findAll();
    }

    public CertificationStatus saveStatus(
            CertificationStatus status
    ) {
        return certificationStatusRepo.save(status);
    }

    public void deleteStatus(UUID id) {
        certificationStatusRepo.deleteById(id);
    }

    public CertificationStatus getStatusById(UUID id) {
        return certificationStatusRepo.findById(id)
                .orElse(null);
    }
}