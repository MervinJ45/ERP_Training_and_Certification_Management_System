package com.example.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(@Value("${cloudinary.cloud-name}") String cloudName, @Value("${cloudinary.api-key}") String apiKey, @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
    }

    public String uploadCertificate(byte[] pdfBytes, String certificateNumber) {
        try {
            Map params = ObjectUtils.asMap("public_id", "certificates/CERT_" + certificateNumber, "resource_type", "raw", "format", "pdf");
            Map uploadResult = cloudinary.uploader().upload(pdfBytes, params);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload certificate file to Cloudinary", e);
        }
    }
}