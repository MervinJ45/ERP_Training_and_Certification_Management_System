package com.example.view.employeeview;

import com.example.dto.CertificationDisplayDTO;
import com.example.entity.User;
import com.example.service.*;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "my-certificates", layout = MainLayout.class)
@PageTitle("ERP | My Certificates")
@RolesAllowed({"EMPLOYEE", "TRAINER", "ADMIN"})
public class MyCertificatesView extends VerticalLayout {

    private final CertificationService certificationService;
    private final CertificationRenewalService renewalService;
    private final CertificationStatusService certificationStatusService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<CertificationDisplayDTO> grid = new Grid<>();
    private String uploadedCloudinaryUrl = null;

    public MyCertificatesView(CertificationService certificationService, CertificationRenewalService renewalService, CloudinaryStorageService cloudinaryStorageService, CurrentUserProvider currentUserProvider, CertificationStatusService certificationStatusService) {
        this.certificationService = certificationService;
        this.renewalService = renewalService;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.currentUserProvider = currentUserProvider;
        this.certificationStatusService = certificationStatusService;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("My Certificates");

        configureGrid();
        loadUserData();

        add(title, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(CertificationDisplayDTO::getCertificateNumber).setHeader("Certificate No.").setSortable(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Certified Skill / Course").setSortable(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        grid.addColumn(dto -> dto.getIssueDate() != null ? dto.getIssueDate().format(formatter) : "N/A").setHeader("Issue Date");
        grid.addColumn(dto -> dto.getExpiryDate() != null ? dto.getExpiryDate().format(formatter) : "Permanent").setHeader("Expiry Date").setSortable(true);

        grid.addComponentColumn(certification -> {
            Span statusBadge = new Span(certification.getStatusName());

            String statusName = certification.getStatusName();
            statusBadge.getElement().setAttribute("style", certificationStatusService.getBadgeColorByStatusId(statusName));

            statusBadge.getElement().getThemeList().add("badge");

            return statusBadge;
        }).setHeader("Validity Status");

        grid.addColumn(new ComponentRenderer<>(dto -> {
            Button downloadBtn = new Button("Download");
            downloadBtn.setIcon(VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Anchor anchor = new Anchor();
            anchor.add(downloadBtn);

            if (dto.getCertificateUrl() != null && !dto.getCertificateUrl().trim().isEmpty()) {
                anchor.setHref(dto.getCertificateUrl());
                anchor.setTarget("_blank");
                anchor.getElement().setAttribute("download", "Certificate_" + dto.getCertificateNumber() + ".pdf");
            } else {
                downloadBtn.setEnabled(false);
                downloadBtn.setText("No File Available");
                downloadBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }

            return anchor;
        })).setHeader("Actions").setAutoWidth(true);

        grid.addItemDoubleClickListener(event -> {
            CertificationDisplayDTO selectedCert = event.getItem();

            if (selectedCert.getExpiryDate() != null && selectedCert.getDaysRemaining() <= 0) {
                openRenewalDialog(selectedCert);
            } else {
                Notification.show("This certificate is still active or permanent. Renewal is not required.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void openRenewalDialog(CertificationDisplayDTO dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Submit Certificate Renewal");
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Span info = new Span("Upload the renewed documentation copy for: " + dto.getCourseName() + " (only pdf files)");
        info.getStyle().set("font-size", "14px").set("color", "var(--lumo-secondary-text-color)");


        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf");
        upload.setMaxFiles(1);
        upload.setWidthFull();

        uploadedCloudinaryUrl = null;

        upload.addSucceededListener(event -> {
            try {
                InputStream is = buffer.getInputStream();
                byte[] fileBytes = is.readAllBytes();

                uploadedCloudinaryUrl = cloudinaryStorageService.uploadCertificate(fileBytes, dto.getCertificateNumber() + "_RENEW");

                Notification.show("File uploaded and encrypted safely to cloud network storage.", 3000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Cloud transmission error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        layout.add(info, upload);

        Button submitBtn = new Button("Submit Request", e -> {
            if (uploadedCloudinaryUrl == null) {
                Notification.show("Please upload a valid certificate document file copy first.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            try {
                Long currentEmpId = currentUserProvider.getCurrentUser().getEmployee().getEmployeeId();

                renewalService.submitRenewalRequest(dto.getCertificationId(), currentEmpId, uploadedCloudinaryUrl);

                Notification n = Notification.show("Renewal request successfully submitted for review!");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadUserData();
            } catch (Exception ex) {
                Notification.show("ERP save fault boundary block triggered: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        submitBtn.setWidthFull();

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.setWidthFull();

        dialog.add(layout);
        dialog.getFooter().add(submitBtn, cancelBtn);
        dialog.open();
    }

    private void loadUserData() {
        User user = currentUserProvider.getCurrentUser();
        Long employeeId = user.getEmployee().getEmployeeId();
        List<CertificationDisplayDTO> personalCertificates = certificationService.getMyCertifications(employeeId);
        grid.setItems(personalCertificates);

        if (personalCertificates.isEmpty()) {
            Notification.show("No active certificates found for your profile.");
        }
    }
}