package com.example.view.superadminview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.service.TrainingEnrollmentService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "training-enrollments", layout = MainLayout.class)
@PageTitle("Training Enrollments")
@RolesAllowed("SUPER_ADMIN")
public class TrainingEnrollmentListView extends VerticalLayout {

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);
    private final TrainingEnrollmentService trainingEnrollmentService;

    public TrainingEnrollmentListView(TrainingEnrollmentService trainingEnrollmentService) {
        this.trainingEnrollmentService = trainingEnrollmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        add(grid);

        loadGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName)
                .setHeader("Employee")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(TrainingEnrollmentDTO::getCourseName)
                .setHeader("Course")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(
                        TrainingEnrollmentDTO::getRequestedCost,
                        NumberFormat.getCurrencyInstance(new  Locale("en", "IN"))))
                .setHeader("Requested Cost")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(
                        TrainingEnrollmentDTO::getApprovedCost,
                        NumberFormat.getCurrencyInstance(new Locale("en", "IN"))))
                .setHeader("Approved Cost")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(TrainingEnrollmentDTO::getCurrentApprovalLevel)
                .setHeader("Approval Level")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new LocalDateTimeRenderer<>(
                        TrainingEnrollmentDTO::getEnrollmentDate, "yyyy-MM-dd HH:mm"))
                .setHeader("Enrollment Date")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Status")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createCertificateBadge))
                .setHeader("Certificate")
                .setAutoWidth(true);

    }

    private Span createStatusBadge(TrainingEnrollmentDTO dto) {
        Span badge = new Span();
        String status = dto.getEnrollmentStatusName() != null ? dto.getEnrollmentStatusName().toUpperCase() : "UNKNOWN";
        badge.setText(dto.getEnrollmentStatusName() != null ? dto.getEnrollmentStatusName() : "Unknown");

        switch (status) {
            case "APPROVED":
            case "COMPLETED":
                badge.getElement().getThemeList().add("badge success");
                break;
            case "PENDING":
            case "PENDING APPROVAL":
                badge.getElement().getThemeList().add("badge warning");
                break;
            case "REJECTED":
            case "CANCELLED":
                badge.getElement().getThemeList().add("badge error");
                break;
            default:
                badge.getElement().getThemeList().add("badge contrast");
                break;
        }
        return badge;
    }

    private Span createCertificateBadge(TrainingEnrollmentDTO dto) {
        Span badge = new Span();
        if (Boolean.TRUE.equals(dto.getCertificateIssued())) {
            badge.setText("Issued");
            badge.getElement().getThemeList().add("badge success primary");
        } else {
            badge.setText("Pending Issue");
            badge.getElement().getThemeList().add("badge warning");
        }
        return badge;
    }

    public void loadGrid() {
        grid.setItems(trainingEnrollmentService.getAllEnrollmentDTOs());
    }
}