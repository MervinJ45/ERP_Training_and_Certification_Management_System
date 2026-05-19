package com.example.view.superadminview;

import com.example.dto.CertificationDisplayDTO;
import com.example.service.CertificationService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;

@Route(value = "certifications", layout = MainLayout.class)
@PageTitle("Certifications")
@RolesAllowed("SUPER_ADMIN")
public class CertificationListView extends VerticalLayout {

    private final Grid<CertificationDisplayDTO> grid = new Grid<>(CertificationDisplayDTO.class, false);
    private final CertificationService certificationService;

    public CertificationListView(CertificationService certificationService) {
        this.certificationService = certificationService;

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

        grid.addColumn(CertificationDisplayDTO::getCertificateNumber).setHeader("Certificate Number").setSortable(true).setAutoWidth(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Course Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(new LocalDateRenderer<>(CertificationDisplayDTO::getIssueDate, "yyyy-MM-dd")).setHeader("Issue Date").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createExpiryDateLayout)).setHeader("Expiry Date").setSortable(true).setComparator(Comparator.comparing(CertificationDisplayDTO::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createDaysRemainingLayout)).setHeader("Days Remaining").setSortable(true).setComparator(Comparator.comparing(CertificationDisplayDTO::getDaysRemaining, Comparator.nullsLast(Comparator.naturalOrder()))).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge)).setHeader("Status").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createActionButtons)).setHeader("Actions").setAutoWidth(true);
    }

    private Component createExpiryDateLayout(CertificationDisplayDTO dto) {
        Span span = new Span();
        if (dto.getExpiryDate() == null) {
            span.setText("Never Expires");
            span.getStyle().set("color", "var(--lumo-secondary-text-color)");
            span.getStyle().set("font-style", "italic");
        } else {
            span.setText(dto.getExpiryDate().toString());
        }
        return span;
    }

    private Component createDaysRemainingLayout(CertificationDisplayDTO dto) {
        Span span = new Span();

        if (dto.getExpiryDate() == null || dto.getDaysRemaining() == null) {
            span.setText("N/A");
            span.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return span;
        }

        if (dto.getDaysRemaining() < 0) {
            span.setText("Expired");
            span.getStyle().set("color", "var(--lumo-error-text-color)");
            span.getStyle().set("font-weight", "bold");
        } else {
            span.setText(dto.getDaysRemaining() + " Days");
            if (dto.getDaysRemaining() <= 30) {
                span.getStyle().set("color", "var(--lumo-warning-text-color)");
            }
        }
        return span;
    }

    private Span createStatusBadge(CertificationDisplayDTO dto) {
        Span badge = new Span();
        String status = dto.getStatusName() != null ? dto.getStatusName().trim().toUpperCase() : "UNKNOWN";
        badge.setText(dto.getStatusName() != null ? dto.getStatusName() : "Unknown");

        switch (status) {
            case "ACTIVE":
                badge.getElement().getThemeList().add("badge success");
                break;
            case "EXPIRED":
                badge.getElement().getThemeList().add("badge error");
                break;
        }
        return badge;
    }

    private Component createActionButtons(CertificationDisplayDTO dto) {
        if (dto.getCertificateUrl() == null || dto.getCertificateUrl().isBlank()) {
            Span noFile = new Span("No File");
            noFile.getStyle().set("color", "var(--lumo-disabled-text-color)");
            noFile.getStyle().set("font-size", "var(--lumo-font-size-s)");
            return noFile;
        }

        Button downloadBtn = new Button("View", VaadinIcon.FILE_TEXT.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Anchor anchor = new Anchor(dto.getCertificateUrl(), "");
        anchor.setTarget("_blank");
        anchor.add(downloadBtn);

        return anchor;
    }

    public void loadGrid() {
        grid.setItems(certificationService.getAllCertificationDTOs());
    }
}