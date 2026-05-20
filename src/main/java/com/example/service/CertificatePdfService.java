package com.example.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CertificatePdfService {

    private static final Logger logger = LoggerFactory.getLogger(CertificatePdfService.class);

    public byte[] generateCertificatePdf(String studentName, String courseName, String certNumber, String issueDate) {

        logger.info("Generating certificate PDF for student: {}", studentName);

        String htmlTemplate = """
                <head>
                    <style>
                        @page { size: letter landscape; margin: 0; }
                        body { font-family: 'Helvetica', Arial, sans-serif; text-align: center; padding: 60px; border: 25px solid #1A365D; background-color: #F8FAFC; }
                        .title { color: #1A365D; font-size: 46px; font-weight: bold; margin-top: 40px; text-transform: uppercase; letter-spacing: 2px; }
                        .subtitle { font-size: 18px; color: #64748B; margin: 20px 0; font-style: italic; }
                        .name { font-size: 36px; font-weight: bold; color: #C2410C; margin: 20px 0; padding: 10px; border-bottom: 2px solid #E2E8F0; display: inline-block; }
                        .course { font-size: 24px; color: #334155; font-weight: 600; margin: 15px 0; }
                        .footer-meta { margin-top: 80px; display: flex; justify-content: space-around; font-size: 14px; color: #64748B; }
                        .cert-num { font-family: monospace; font-size: 16px; color: #475569; margin-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="title">Certificate of Completion</div>
                    <div class="subtitle">This is proudly presented to</div>
                    <div class="name">%s</div>
                    <div class="subtitle">for successfully completing the specialized training program</div>
                    <div class="course">%s</div>
                
                    <table style="width:100%%; margin-top:80px; font-size:14px; color:#64748B;">
                        <tr>
                            <td style="text-align: left; padding-left: 50px;">
                                <strong>Issue Date:</strong> %s
                            </td>
                            <td style="text-align: right; padding-right: 50px;">
                                <strong>Certificate ID:</strong> <span class="cert-num">%s</span>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

        String formattedHtml = String.format(htmlTemplate, studentName, courseName, issueDate, certNumber);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(formattedHtml, "/");
            builder.toStream(os);
            builder.run();

            logger.info("Successfully generated certificate PDF for certificate number: {}", certNumber);

            return os.toByteArray();
        } catch (Exception e) {

            logger.error("Failed to generate certificate PDF for certificate number: {}", certNumber, e);

            throw new RuntimeException("Failed to render Certificate PDF properties", e);
        }
    }
}