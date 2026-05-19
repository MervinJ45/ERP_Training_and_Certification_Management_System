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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "my-approvals", layout = MainLayout.class)
@PageTitle("ERP | My Approval History")
@RolesAllowed({"HR", "DIRECTOR", "MANAGER"})
public class MyApprovalsView extends VerticalLayout {

    private final TrainingApprovalService approvalService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingApprovalDTO> grid = new Grid<>(TrainingApprovalDTO.class, false);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyApprovalsView(TrainingApprovalService approvalService, CurrentUserProvider currentUserProvider) {
        this.approvalService = approvalService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();

        H2 title = new H2("My Approval History");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {
        grid.addColumn(TrainingApprovalDTO::getEnrollmentId).setHeader("Enrollment ID").setAutoWidth(true);
        grid.addColumn(TrainingApprovalDTO::getCourseName).setHeader("Course").setAutoWidth(true);
        grid.addColumn(TrainingApprovalDTO::getEmployeeFullName).setHeader("Requester").setAutoWidth(true);
        grid.addColumn(TrainingApprovalDTO::getApprovalLevel).setHeader("Level").setAutoWidth(true);
        grid.addColumn(TrainingApprovalDTO::getApprovalStatusName).setHeader("Status").setAutoWidth(true);
        grid.addColumn(TrainingApprovalDTO::getComments).setHeader("Comments").setAutoWidth(true);

        grid.addColumn(dto -> dto.getActionDate() != null ? dto.getActionDate().format(DATE_FORMATTER) : "-").setHeader("Action Date").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private void loadData() {
        User user = currentUserProvider.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            Employee currentEmployee = user.getEmployee();
            grid.setItems(approvalService.getApprovalsByApprover(currentEmployee.getEmployeeId()));
        }
    }
}