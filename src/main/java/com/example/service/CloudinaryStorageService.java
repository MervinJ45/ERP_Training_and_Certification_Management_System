package com.example.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(@Value("${cloudinary.cloud-name}") String cloudName, @Value("${cloudinary.api-key}") String apiKey, @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
    }

    public String uploadCertificate(byte[] pdfBytes, String certificateNumber) {

        try {

            logger.info("Uploading certificate PDF to Cloudinary for certificate number: {}", certificateNumber);

            Map params = ObjectUtils.asMap(
                    "public_id", "certificates/CERT_" + certificateNumber,
                    "resource_type", "raw",
                    "format", "pdf"
            );

            Map uploadResult = cloudinary.uploader().upload(pdfBytes, params);

            logger.info("Certificate uploaded successfully for certificate number: {}", certificateNumber);

            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {

            logger.error("Failed to upload certificate file to Cloudinary for certificate number: {}", certificateNumber, e);

            throw new RuntimeException("Failed to upload certificate file to Cloudinary", e);
        }
    }
}