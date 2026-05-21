package com.example.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CertificateExpiryEmailService {

    private final JavaMailSender mailSender;

    CertificateExpiryEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendExpiryEmail(String toEmail, String employeeName, String courseName, String certNumber) {
        String subject = "Action Required: Your Certificate has Expired";
        String text = String.format("Dear %s,\n\n" + "This is a reminder that your certificate for the course '%s' (Certificate No: %s) has expired.\n" + "Please log into the system and submit a renewal request as soon as possible.\n\n" + "Best regards,\nTraining Management System", employeeName, courseName, certNumber);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}