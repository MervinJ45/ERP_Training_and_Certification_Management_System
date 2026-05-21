package com.example.view.superadminview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.service.TrainingEnrollmentService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "training-enrollments", layout = MainLayout.class)
@PageTitle("Training Enrollments")
@RolesAllowed("SUPER_ADMIN")
public class TrainingEnrollmentListView extends VerticalLayout {

    private final TrainingEnrollmentService trainingEnrollmentService;
    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    private final TextField employeeFilter = new TextField();
    private final TextField courseFilter = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    private List<TrainingEnrollmentDTO> allEnrollments;

    public TrainingEnrollmentListView(TrainingEnrollmentService trainingEnrollmentService) {
        this.trainingEnrollmentService = trainingEnrollmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Training Enrollments Management");
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

        courseFilter.setPlaceholder("Filter by Course Track");
        courseFilter.setClearButtonVisible(true);
        courseFilter.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());
        courseFilter.setWidth("240px");
        courseFilter.setValueChangeMode(ValueChangeMode.LAZY);
        courseFilter.addValueChangeListener(e -> filterGrid());

        statusFilter.setPlaceholder("Status (All)");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");
        statusFilter.setItems("Approved", "Completed", "Pending Approval", "Rejected", "Certified");
        statusFilter.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setSortable(true).setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course").setSortable(true).setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(TrainingEnrollmentDTO::getRequestedCost, NumberFormat.getCurrencyInstance(new Locale("en", "IN")))).setHeader("Requested Cost").setSortable(true).setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(TrainingEnrollmentDTO::getApprovedCost, NumberFormat.getCurrencyInstance(new Locale("en", "IN")))).setHeader("Approved Cost").setSortable(true).setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCurrentApprovalLevel).setHeader("Approval Level").setSortable(true).setAutoWidth(true);
        grid.addColumn(new LocalDateTimeRenderer<>(TrainingEnrollmentDTO::getEnrollmentDate, "yyyy-MM-dd HH:mm")).setHeader("Enrollment Date").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge)).setHeader("Status").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createCertificateBadge)).setHeader("Certificate").setAutoWidth(true);
    }

    private void loadInitialData() {
        allEnrollments = trainingEnrollmentService.getAllEnrollmentDTOs();
        filterGrid();
    }


    private void filterGrid() {
        if (allEnrollments == null) return;

        String empQuery = employeeFilter.getValue() != null ? employeeFilter.getValue().trim().toLowerCase() : "";
        String courseQuery = courseFilter.getValue() != null ? courseFilter.getValue().trim().toLowerCase() : "";
        String statusQuery = statusFilter.getValue();

        if (empQuery.isEmpty() && courseQuery.isEmpty() && statusQuery == null) {
            grid.setItems(allEnrollments);
            return;
        }

        List<TrainingEnrollmentDTO> filteredList = allEnrollments.stream().filter(dto -> {
            boolean matchesEmployee = empQuery.isEmpty() || (dto.getEmployeeFullName() != null && dto.getEmployeeFullName().toLowerCase().contains(empQuery));
            boolean matchesCourse = courseQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));
            boolean matchesStatus = statusQuery == null || (dto.getEnrollmentStatusName() != null && dto.getEnrollmentStatusName().equalsIgnoreCase(statusQuery));
            return matchesEmployee && matchesCourse && matchesStatus;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }

    private Span createStatusBadge(TrainingEnrollmentDTO dto) {
        Span badge = new Span();
        String status = dto.getEnrollmentStatusName() != null ? dto.getEnrollmentStatusName().toUpperCase() : "UNKNOWN";
        badge.setText(dto.getEnrollmentStatusName() != null ? dto.getEnrollmentStatusName() : "Unknown");

        badge.getElement().getThemeList().add("badge");

        switch (status) {
            case "APPROVED":
            case "COMPLETED":
                badge.getElement().getThemeList().add("success");
                break;
            case "PENDING APPROVAL":
                badge.getElement().getThemeList().add("warning");
                break;
            case "REJECTED":
            case "CANCELLED":
                badge.getElement().getThemeList().add("error");
                break;
            default:
                badge.getElement().getThemeList().add("contrast");
                break;
        }
        return badge;
    }

    private Span createCertificateBadge(TrainingEnrollmentDTO dto) {
        Span badge = new Span();
        badge.getElement().getThemeList().add("badge");

        if (Boolean.TRUE.equals(dto.getCertificateIssued())) {
            badge.setText("Issued");
            badge.getElement().getThemeList().add("success");
            badge.getElement().getThemeList().add("primary");
        } else {
            badge.setText("Pending Issue");
            badge.getElement().getThemeList().add("warning");
        }
        return badge;
    }
}