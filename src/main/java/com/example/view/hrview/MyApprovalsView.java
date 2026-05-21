package com.example.view.hrview;

import com.example.dto.TrainingApprovalDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.TrainingApprovalService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "my-approvals", layout = MainLayout.class)
@PageTitle("ERP | My Approval History")
@RolesAllowed({"HR", "DIRECTOR", "MANAGER"})
public class MyApprovalsView extends VerticalLayout {

    private final TrainingApprovalService approvalService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingApprovalDTO> grid = new Grid<>(TrainingApprovalDTO.class, false);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TextField requesterSearchField = new TextField();
    private final TextField courseSearchField = new TextField();
    private final Select<String> statusSelectField = new Select<>();

    private List<TrainingApprovalDTO> allData;

    public MyApprovalsView(TrainingApprovalService approvalService, CurrentUserProvider currentUserProvider) {
        this.approvalService = approvalService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Approval History");

        configureRequesterSearch();
        configureCourseSearch();
        configureStatusFilter();
        configureGrid();

        HorizontalLayout filterActionLayout = new HorizontalLayout(requesterSearchField, courseSearchField, statusSelectField);
        filterActionLayout.setWidthFull();
        filterActionLayout.setSpacing(true);

        grid.setSizeFull();

        add(title, filterActionLayout, grid);
        loadData();
    }

    private void configureRequesterSearch() {
        requesterSearchField.setPlaceholder("Search by Requester");
        requesterSearchField.setClearButtonVisible(true);
        requesterSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        requesterSearchField.setWidth("240px");
        requesterSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        requesterSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureCourseSearch() {
        courseSearchField.setPlaceholder("Search by Course");
        courseSearchField.setClearButtonVisible(true);
        courseSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        courseSearchField.setWidth("240px");
        courseSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        courseSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureStatusFilter() {
        statusSelectField.setPlaceholder("Status (All)");
        statusSelectField.setEmptySelectionAllowed(true);
        statusSelectField.setEmptySelectionCaption("Status (All)");
        statusSelectField.setItems("Approved", "Rejected");
        statusSelectField.setWidth("200px");
        statusSelectField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TrainingApprovalDTO::getEnrollmentId).setHeader("Enrollment ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingApprovalDTO::getCourseName).setHeader("Course").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingApprovalDTO::getEmployeeFullName).setHeader("Requester").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(this::createApprovalStatusBadge))
                .setHeader("Status")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(TrainingApprovalDTO::getComments).setHeader("Comments").setAutoWidth(true);
        grid.addColumn(dto -> dto.getActionDate() != null ? dto.getActionDate().format(DATE_FORMATTER) : "-").setHeader("Action Date & Time").setAutoWidth(true).setSortable(true);
    }


    private Span createApprovalStatusBadge(TrainingApprovalDTO dto) {
        String status = dto.getApprovalStatusName() != null ? dto.getApprovalStatusName().trim().toUpperCase() : "UNKNOWN";
        Span badge = new Span(dto.getApprovalStatusName() != null ? dto.getApprovalStatusName() : "Unknown");

        badge.getElement().getThemeList().add("badge");

        switch (status) {
            case "APPROVED":
                badge.getElement().getThemeList().add("success");
                break;
            case "REJECTED":
                badge.getElement().getThemeList().add("error");
                break;
            default:
                badge.getElement().getThemeList().add("contrast");
                break;
        }
        return badge;
    }

    private void loadData() {
        User user = currentUserProvider.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            Employee currentEmployee = user.getEmployee();
            allData = approvalService.getApprovalsByApprover(currentEmployee.getEmployeeId());
            grid.setItems(allData);
        }
    }

    private void filterGrid() {
        if (allData == null) return;

        String requesterQuery = requesterSearchField.getValue() != null ? requesterSearchField.getValue().trim().toLowerCase() : "";
        String courseQuery = courseSearchField.getValue() != null ? courseSearchField.getValue().trim().toLowerCase() : "";
        String statusQuery = statusSelectField.getValue();

        if (requesterQuery.isEmpty() && courseQuery.isEmpty() && statusQuery == null) {
            grid.setItems(allData);
            return;
        }

        List<TrainingApprovalDTO> filteredList = allData.stream().filter(dto -> {
            boolean matchesRequester = requesterQuery.isEmpty() || (dto.getEmployeeFullName() != null && dto.getEmployeeFullName().toLowerCase().contains(requesterQuery));

            boolean matchesCourse = courseQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));

            boolean matchesStatus = statusQuery == null || (dto.getApprovalStatusName() != null && dto.getApprovalStatusName().equalsIgnoreCase(statusQuery));

            return matchesRequester && matchesCourse && matchesStatus;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }
}