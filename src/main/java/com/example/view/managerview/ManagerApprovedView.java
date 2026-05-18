package com.example.view.managerview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.TrainingEnrollmentService;
import com.example.service.UserService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "manager-approved", layout = MainLayout.class)
@PageTitle("ERP | Approved Enrollments")
@RolesAllowed({"MANAGER", "ADMIN"})
public class ManagerApprovedView extends VerticalLayout {

    private final TrainingEnrollmentService enrollmentService;
    private final AuthenticationContext authContext;
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public ManagerApprovedView(TrainingEnrollmentService enrollmentService, AuthenticationContext authContext, UserService userService, CurrentUserProvider  currentUserProvider) {

        this.enrollmentService = enrollmentService;
        this.authContext = authContext;
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();

        H2 title = new H2("Enrollments Approved By Me");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getRequestedCost).setHeader("Requested Cost").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getApprovedCost).setHeader("Approved Cost").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCurrentApprovalLevel).setHeader("Current Approval Level").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Status").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private void loadData() {
        User user = currentUserProvider.getCurrentUser();
        Employee manager = user.getEmployee();
        grid.setItems(enrollmentService.getEnrollmentsApprovedByManager(manager.getEmployeeId()));
    }
}