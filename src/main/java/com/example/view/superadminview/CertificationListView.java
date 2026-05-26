package com.example.view.superadminview;

import com.example.dto.CertificationDisplayDTO;
import com.example.service.CertificationService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "certifications", layout = MainLayout.class)
@PageTitle("Certifications")
@RolesAllowed("SUPER_ADMIN")
public class CertificationListView extends VerticalLayout {

    private final Grid<CertificationDisplayDTO> grid = new Grid<>(CertificationDisplayDTO.class, false);
    private final CertificationService certificationService;

    private final TextField employeeFilter = new TextField();
    private final TextField courseFilter = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    private List<CertificationDisplayDTO> allCertifications;

    public CertificationListView(CertificationService certificationService) {
        this.certificationService = certificationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Certifications");
        title.getStyle().set("margin-top", "0");

        configureFilters();
        configureGrid();

        HorizontalLayout toolbar = new HorizontalLayout(employeeFilter, courseFilter, statusFilter);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);

        add(title, toolbar, grid);
        expand(grid);

        loadInitialData();
    }

    private void configureFilters() {
        employeeFilter.setPlaceholder("Filter by Employee Name");
        employeeFilter.setClearButtonVisible(true);
        employeeFilter.setPrefixComponent(VaadinIcon.USER.create());
        employeeFilter.setWidth("240px");
        employeeFilter.setValueChangeMode(ValueChangeMode.LAZY);
        employeeFilter.addValueChangeListener(e -> filterGrid());

        courseFilter.setPlaceholder("Filter by Course Name");
        courseFilter.setClearButtonVisible(true);
        courseFilter.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());
        courseFilter.setWidth("240px");
        courseFilter.setValueChangeMode(ValueChangeMode.LAZY);
        courseFilter.addValueChangeListener(e -> filterGrid());

        statusFilter.setPlaceholder("Status (All)");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");
        statusFilter.setItems("Active", "Expired", "Renewed");
        statusFilter.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(CertificationDisplayDTO::getCertificateNumber).setHeader("Certificate Number").setSortable(true).setAutoWidth(true);
        grid.addColumn(CertificationDisplayDTO::getEmployee).setHeader("Employee Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Course Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(new LocalDateRenderer<>(CertificationDisplayDTO::getIssueDate, "yyyy-MM-dd")).setHeader("Issue Date").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createExpiryDateLayout)).setHeader("Expiry Date").setSortable(true).setComparator(Comparator.comparing(CertificationDisplayDTO::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge)).setHeader("Status").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createActionButtons)).setHeader("Actions").setAutoWidth(true);
    }

    private void loadInitialData() {
        allCertifications = certificationService.getAllCertificationDTOs();
        filterGrid();
    }

    private void filterGrid() {
        if (allCertifications == null) return;

        String empQuery = employeeFilter.getValue() != null ? employeeFilter.getValue().trim().toLowerCase() : "";
        String courseQuery = courseFilter.getValue() != null ? courseFilter.getValue().trim().toLowerCase() : "";
        String statusQuery = statusFilter.getValue();

        if (empQuery.isEmpty() && courseQuery.isEmpty() && statusQuery == null) {
            grid.setItems(allCertifications);
            return;
        }

        List<CertificationDisplayDTO> filteredList = allCertifications.stream().filter(dto -> {
            boolean matchesEmployee = empQuery.isEmpty() || (dto.getEmployee() != null && dto.getEmployee().toLowerCase().contains(empQuery));

            boolean matchesCourse = courseQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));

            boolean matchesStatus = statusQuery == null || (dto.getStatusName() != null && dto.getStatusName().equalsIgnoreCase(statusQuery));

            return matchesEmployee && matchesCourse && matchesStatus;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
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

    private Span createStatusBadge(CertificationDisplayDTO dto) {
        Span badge = new Span();
        String status = dto.getStatusName() != null ? dto.getStatusName().trim().toUpperCase() : "UNKNOWN";
        badge.setText(dto.getStatusName() != null ? dto.getStatusName() : "Unknown");

        badge.getElement().getThemeList().add("badge");

        switch (status) {
            case "ACTIVE":
                badge.getElement().getThemeList().add("success");
                break;
            case "EXPIRED":
                badge.getElement().getThemeList().add("error");
                break;
            case "RENEWED":
                badge.getStyle().set("background-color", "#e0e7ff");
                badge.getStyle().set("color", "#4338ca");
                break;
            default:
                badge.getElement().getThemeList().add("contrast");
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
        loadInitialData();
    }
}