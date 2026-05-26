package com.example.view.employeeview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Certification;
import com.example.service.CertificationService;
import com.example.service.TrainingEnrollmentService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "certificate-history", layout = MainLayout.class)
@PageTitle("ERP | Certificate Renewal History")
@RolesAllowed("EMPLOYEE")
public class CertificateHistoryView extends VerticalLayout {

    private final TrainingEnrollmentService enrollmentService;
    private final CertificationService certificationService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>();

    public CertificateHistoryView(TrainingEnrollmentService enrollmentService, CertificationService certificationService, CurrentUserProvider currentUserProvider) {
        this.enrollmentService = enrollmentService;
        this.certificationService = certificationService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 heading = new H2("My Enrolled Trainings");
        heading.getStyle().set("margin-top", "0");

        configureEnrollmentGrid();
        loadUserEnrollments();

        add(heading, new Hr(), grid);
    }

    private void configureEnrollmentGrid() {
        grid.setSizeFull();

        grid.addColumn(TrainingEnrollmentDTO::getCourseName)
                .setHeader("Course Name")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(dto -> dto.getEnrollmentDate() != null ?
                        dto.getEnrollmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "N/A")
                .setHeader("Enrollment Date")
                .setAutoWidth(true);

        grid.addColumn(dto -> dto.getCompletionDate() != null ?
                        dto.getCompletionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "In Progress")
                .setHeader("Completed On")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createEnrollmentStatusBadge)
                .setHeader("Course Status")
                .setAutoWidth(true);

        grid.setDetailsVisibleOnClick(true);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createCertificateGridDropdownContent));
    }


    private VerticalLayout createCertificateGridDropdownContent(TrainingEnrollmentDTO enrollment) {
        VerticalLayout dropdownContainer = new VerticalLayout();
        dropdownContainer.setPadding(true);
        dropdownContainer.setSpacing(true);
        dropdownContainer.getStyle()
                .set("background-color", "#F8FAFC")
                .set("border-left", "4px solid #1A365D")
                .set("margin", "5px 0px");

        Span dropdownHeader = new Span(VaadinIcon.FILE_TEXT.create());
        dropdownHeader.add(new Span(" Issued Certificates :"));
        dropdownHeader.getStyle().set("font-weight", "bold").set("color", "#1A365D");
        dropdownContainer.add(dropdownHeader);

        List<Certification> associatedCertificates = certificationService.getCertificationsByEnrollmentId(enrollment.getEnrollmentId());

        if (associatedCertificates.isEmpty()) {
            Span noCertsMessage = new Span("No certificates have been issued for this training yet.");
            noCertsMessage.getStyle().set("font-style", "italic").set("color", "#94A3B8").set("padding-left", "5px");
            dropdownContainer.add(noCertsMessage);
        } else {
            Grid<Certification> certGrid = new Grid<>();
            certGrid.setAllRowsVisible(true);
            certGrid.setItems(associatedCertificates);

            certGrid.addColumn(Certification::getCertificateNumber)
                    .setHeader("Certificate Code")
                    .setAutoWidth(true);

            certGrid.addColumn(cert -> cert.getIssueDate() != null ?
                            cert.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "")
                    .setHeader("Issue Date")
                    .setAutoWidth(true);

            certGrid.addColumn(cert -> cert.getExpiryDate() != null ?
                            cert.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Never")
                    .setHeader("Expiry Date")
                    .setAutoWidth(true);

            certGrid.addComponentColumn(this::createCertificationStatusBadge)
                    .setHeader("Validity Status")
                    .setAutoWidth(true);

            dropdownContainer.add(certGrid);
        }

        return dropdownContainer;
    }

    private Span createEnrollmentStatusBadge(TrainingEnrollmentDTO enrollment) {
        String status = enrollment.getEnrollmentStatusName() != null ? enrollment.getEnrollmentStatusName() : "Pending";
        Span badge = new Span(status);
        badge.getElement().getThemeList().add("badge");

        String style = switch (status) {
            case "Certified", "Completed" -> "background-color: #DCFCE7; color: #15803D;";
            case "Approved" -> "background-color: #EFF6FF; color: #1D4ED8;";
            case "Rejected" -> "background-color: #FEE2E2; color: #B91C1C;";
            default -> "background-color: #FEF9C3; color: #854D0E;";
        };
        badge.getElement().setAttribute("style", style);
        return badge;
    }


    private Span createCertificationStatusBadge(Certification cert) {
        String statusName = (cert.getStatus() != null) ? cert.getStatus().getCertificationStatus() : "Active";
        Long statusId = (cert.getStatus() != null) ? cert.getStatus().getCertificationStatusId() : 1L;

        Span badge = new Span(statusName);
        badge.getElement().getThemeList().add("badge");

        String style = switch (statusId.intValue()) {
            case 1 -> "background-color: #DCFCE7; color: #15803D; font-weight: bold;";
            case 2 -> "background-color: #FEE2E2; color: #B91C1C; font-weight: bold;";
            case 4 -> "background-color: #e0e7ff; color: #4338ca; font-weight: bold;";
            default -> "background-color: #F1F5F9; color: #475569;";
        };
        badge.getElement().setAttribute("style", style);
        return badge;
    }


    private void loadUserEnrollments() {
        if (currentUserProvider.getCurrentUser() != null && currentUserProvider.getCurrentUser().getEmployee() != null) {
            Long employeeId = currentUserProvider.getCurrentUser().getEmployee().getEmployeeId();
            List<TrainingEnrollmentDTO> userHistory = enrollmentService.getCertifiedEnrollmentsByEmployee(employeeId);
            grid.setItems(userHistory);
        }
    }
}