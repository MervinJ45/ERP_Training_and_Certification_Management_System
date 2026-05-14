package com.example.view;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.EmployeeService;
import com.example.service.TrainingEnrollmentService;
import com.example.service.UserService;
import com.example.utils.CurrentUserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;

@Route(value = "manager-approvals", layout = MainLayout.class)
@PageTitle("ERP | Manager Approvals")
@RolesAllowed({"MANAGER", "ADMIN"})
public class ManagerApprovalView extends VerticalLayout {

    private final TrainingEnrollmentService enrollmentService;
    private final AuthenticationContext authContext;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CurrentUserProvider currentUserProvider;

    private Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public ManagerApprovalView(TrainingEnrollmentService enrollmentService, AuthenticationContext authContext, UserService userService, EmployeeService employeeService, CurrentUserProvider currentUserProvider) {

        this.enrollmentService = enrollmentService;
        this.authContext = authContext;
        this.userService = userService;
        this.employeeService = employeeService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();

        H2 title = new H2("Employee Enrollment Requests");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee");
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course");
        grid.addColumn(TrainingEnrollmentDTO::getRequestedCost).setHeader("Requested Cost");
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Status");
        grid.addComponentColumn(dto -> {

            Button approveBtn = new Button("Approve");
            approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            approveBtn.addClickListener(e -> openApprovalDialog(dto));

            return approveBtn;

        }).setHeader("Approve");

        grid.addComponentColumn(dto -> {

            Button rejectBtn = new Button("Reject");
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            rejectBtn.addClickListener(e -> {
//                enrollmentService.rejectEnrollment(dto.getEnrollmentId());
                Notification.show("Enrollment Rejected");
                loadData();
            });

            return rejectBtn;

        }).setHeader("Reject");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private void openApprovalDialog(TrainingEnrollmentDTO dto) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Approve Enrollment");

        NumberField approvedCost = new NumberField("Approved Cost");
        approvedCost.setValue(dto.getRequestedCost().doubleValue());

        TextArea comments = new TextArea("Manager Comments");
        comments.setPlaceholder("Enter approval remarks here...");
        comments.setHeight("10em");
        comments.setWidthFull();

        Button approveBtn = new Button("Approve", e -> {
            enrollmentService.approveEnrollment(dto.getEnrollmentId(), getCurrentEmployee(authContext).getEmployeeId(), BigDecimal.valueOf(approvedCost.getValue()), comments.getValue());
            Notification.show("Enrollment Approved");
            dialog.close();
            loadData();
        });

        approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(approvedCost, comments);
        dialog.getFooter().add(approveBtn, new Button("Cancel", e -> dialog.close()));
        dialog.open();
    }

    private void loadData() {
        Employee manager = getCurrentEmployee(authContext);
        grid.setItems(enrollmentService.getPendingManagerApprovals(manager.getEmployeeId()));
    }

    public Employee getCurrentEmployee(AuthenticationContext authContext) {

        User user = currentUserProvider.getCurrentUser();

        return user.getEmployee();

    }

}