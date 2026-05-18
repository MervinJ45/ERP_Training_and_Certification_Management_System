package com.example.view.employeeview;

import com.example.dto.CertificationDisplayDTO;
import com.example.entity.User;
import com.example.service.CertificationService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "my-certificates", layout = MainLayout.class)
@PageTitle("ERP | My Certificates")
@RolesAllowed({"EMPLOYEE", "TRAINER", "ADMIN"}) // Protects the route from unauthenticated access
public class MyCertificatesView extends VerticalLayout {

    private final CertificationService certificationService;
    private final CurrentUserProvider currentUserProvider;
    private final Grid<CertificationDisplayDTO> grid = new Grid<>();

    public MyCertificatesView(CertificationService certificationService, CurrentUserProvider currentUserProvider) {
        this.certificationService = certificationService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);

        // Header
        H2 title = new H2("My Professional Qualifications");
        Span subTitle = new Span("View your earned corporate credentials, validity tracking, and digital copies.");
        subTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        configureGrid();
        loadUserData();

        add(title, subTitle, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Column Definitions mapping the schema properties
        grid.addColumn(CertificationDisplayDTO::getCertificateNumber).setHeader("Certificate No.").setSortable(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Certified Skill / Course").setSortable(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        grid.addColumn(dto -> dto.getIssueDate().format(formatter)).setHeader("Issue Date");
        grid.addColumn(dto -> dto.getExpiryDate().format(formatter)).setHeader("Expiry Date").setSortable(true);

        // Dynamic Expiry Tracking Status Badges
        grid.addColumn(new ComponentRenderer<>(dto -> {
            Span badge = new Span();
            long days = dto.getDaysRemaining();

            if (days < 0) {
                badge.setText("Expired (" + Math.abs(days) + " days ago)");
                badge.getElement().getThemeList().add("badge error");
            } else if (days <= 30) {
                badge.setText(days + " Days Left (Expiring Soon)");
                badge.getElement().getThemeList().add("badge warning");
            } else {
                badge.setText(days + " Days Valid");
                badge.getElement().getThemeList().add("badge success");
            }
            return badge;
        })).setHeader("Validity Status").setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void loadUserData() {
        User user = currentUserProvider.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            Notification.show("Session Timeout: Unable to verify profile identity.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Context Isolation: Bind query completely to the validated session ID
        Long employeeId = user.getEmployee().getEmployeeId();

        List<CertificationDisplayDTO> personalCertificates = certificationService.getMyCertifications(employeeId);
        grid.setItems(personalCertificates);

        if (personalCertificates.isEmpty()) {
            Notification.show("No active certificates found for your profile.");
        }
    }
}