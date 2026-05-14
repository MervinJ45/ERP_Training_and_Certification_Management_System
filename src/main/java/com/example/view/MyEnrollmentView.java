package com.example.view;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Employee;
import com.example.service.TrainingEnrollmentService;
import com.example.service.UserService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "my-enrollments", layout = MainLayout.class)
@PageTitle("ERP | My Enrollments")
@RolesAllowed("EMPLOYEE")
public class MyEnrollmentView extends VerticalLayout {

    private final TrainingEnrollmentService enrollmentService;
    private final AuthenticationContext authContext;
    private final UserService userService;

    private Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public MyEnrollmentView(TrainingEnrollmentService enrollmentService, AuthenticationContext authContext, UserService userService) {

        this.enrollmentService = enrollmentService;
        this.authContext = authContext;
        this.userService = userService;

        setSizeFull();

        H2 title = new H2("My Enrollments");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {

        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course");
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentDate).setHeader("Enrollment Date");
        grid.addColumn(TrainingEnrollmentDTO::getRequestedCost).setHeader("Requested Cost");
        grid.addColumn(TrainingEnrollmentDTO::getApprovedCost).setHeader("Approved Cost");
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Status");
        grid.addColumn(TrainingEnrollmentDTO::getCurrentApprovalLevel).setHeader("Approval Level");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.setSizeFull();
    }

    private void loadData() {

        authContext.getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class).ifPresent(userDetails -> {
            userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                Employee employee = user.getEmployee();
                grid.setItems(enrollmentService.getEnrollmentsByEmployee(employee.getEmployeeId()));
            });
        });
    }
}