package com.example.service;

import com.example.entity.Certification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CertificateExpiryEmailService {

    private final JavaMailSender mailSender;

    public CertificateExpiryEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConsolidatedExpiryEmail(String toEmail, String employeeName, List<Certification> upcomingCerts) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⏳ Action Required: Your Certifications Are Expiring Soon");

            LocalDate today = LocalDate.now();
            StringBuilder tableRows = new StringBuilder();

            for (Certification cert : upcomingCerts) {
                LocalDate expiryDate = cert.getExpiryDate().toLocalDate();

                long daysRemaining = ChronoUnit.DAYS.between(today, expiryDate);
                String daysText = daysRemaining == 1 ? "1 day" : daysRemaining + " days";

                tableRows.append(String.format("<tr>" + "  <td style='padding: 12px; border-bottom: 1px solid #e2e8f0; font-weight: 600; color: #1e293b;'>%s</td>" + "  <td style='padding: 12px; border-bottom: 1px solid #e2e8f0; color: #64748b; font-family: monospace;'>%s</td>" + "  <td style='padding: 12px; border-bottom: 1px solid #e2e8f0; color: #64748b;'>%s</td>" + "  <td style='padding: 12px; border-bottom: 1px solid #e2e8f0; color: #f97316; font-weight: 500;'>Expires in %s</td>" + "</tr>", cert.getCourse().getCourseName(), cert.getCertificateNumber(), expiryDate.toString(), daysText));
            }

            String htmlContent = "<html>" + "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; padding: 20px; margin: 0;'>" + "  <div style='max-width: 650px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border: 1px solid #e2e8f0;'>" + "    <div style='background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); padding: 30px; text-align: center;'>" + "      <h2 style='color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;'>Certification Renewal Notice</h2>" + "    </div>" + "    <div style='padding: 30px; color: #334155; line-height: 1.6;'>" + "      <p style='font-size: 16px; margin-top: 0;'>Dear <strong>" + employeeName + "</strong>,</p>" + "      <p style='font-size: 15px; color: #475569;'>This is an automated reminder that the following training certification(s) assigned to your profile <strong>are approaching their expiration dates</strong>:</p>" + "      " + "      <table style='width: 100%; border-collapse: collapse; margin: 25px 0; text-align: left; font-size: 14px;'>" + "        <thead>" + "          <tr style='background-color: #f1f5f9; color: #475569; font-weight: 600;'>" + "            <th style='padding: 12px; border-bottom: 2px solid #e2e8f0;'>Course Title</th>" + "            <th style='padding: 12px; border-bottom: 2px solid #e2e8f0;'>Certificate ID</th>" + "            <th style='padding: 12px; border-bottom: 2px solid #e2e8f0;'>Expiry Date</th>" + "            <th style='padding: 12px; border-bottom: 2px solid #e2e8f0;'>Time Remaining</th>" + "          </tr>" + "        </thead>" + "        <tbody>" + tableRows.toString() + "</tbody>" + "      </table>" + "      " + "      <p style='font-size: 15px; color: #475569; margin-bottom: 0;'>To ensure your compliance status remains uninterrupted, please head over to your Employee Dashboard to re-enroll in the required training modules or submit a renewal request.</p>" + "    </div>" + "    <div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;'>" + "      <p style='margin: 0;'>This is an automated operational system message. Please do not reply directly to this mail context.</p>" + "      <p style='margin: 5px 0 0 0;'>&copy; 2026 Enterprise Resource Planning Engine.</p>" + "    </div>" + "  </div>" + "</body>" + "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to compile or deliver HTML email sequence: " + e.getMessage(), e);
        }
    }
}