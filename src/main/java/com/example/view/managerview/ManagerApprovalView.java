package com.example.view.managerview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.EmployeeService;
import com.example.service.TrainingEnrollmentService;
import com.example.service.UserService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
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
            rejectBtn.addClickListener(e -> openRejectionDialog(dto)); // Fixed to use proper dialog flow
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
        approvedCost.setWidthFull();

        TextArea comments = new TextArea("Manager Comments");
        comments.setPlaceholder("Enter approval remarks here...");
        comments.setHeight("8em");
        comments.setWidthFull();

        Button approveBtn = new Button("Approve", e -> {
            // FIX: Prevent NullPointerException if the field is cleared manually by a user
            BigDecimal finalCost = (approvedCost.getValue() != null) ? BigDecimal.valueOf(approvedCost.getValue()) : dto.getRequestedCost();

            try {
                Long approverId = getCurrentEmployee().getEmployeeId();
                enrollmentService.approveEnrollment(dto.getEnrollmentId(), approverId, finalCost, comments.getValue());

                Notification n = Notification.show("Enrollment Approved Successfully");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Error processing approval: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(approvedCost, comments);
        layout.setPadding(false);
        dialog.add(layout);

        dialog.getFooter().add(approveBtn, new Button("Cancel", e -> dialog.close()));
        dialog.open();
    }

    private void openRejectionDialog(TrainingEnrollmentDTO dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reject Enrollment Request");

        TextArea comments = new TextArea("Reason for Rejection");
        comments.setPlaceholder("Please specify why this training enrollment is being rejected...");
        comments.setHeight("8em");
        comments.setWidthFull();
        comments.setRequired(true);

        Button confirmRejectBtn = new Button("Reject Request", e -> {
            if (comments.getValue().trim().isEmpty()) {
                comments.setInvalid(true);
                comments.setErrorMessage("Rejection comments are mandatory.");
                return;
            }

            try {
                Long approverId = getCurrentEmployee().getEmployeeId();
                enrollmentService.rejectEnrollment(dto.getEnrollmentId(), approverId, comments.getValue());

                Notification n = Notification.show("Enrollment Request Rejected");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Error processing rejection: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmRejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(comments);
        layout.setPadding(false);
        dialog.add(layout);

        dialog.getFooter().add(confirmRejectBtn, new Button("Cancel", e -> dialog.close()));
        dialog.open();
    }

    private void loadData() {
        Employee manager = getCurrentEmployee();
        if (manager != null) {
            grid.setItems(enrollmentService.getPendingManagerApprovals(manager.getEmployeeId()));
        }
    }

    public Employee getCurrentEmployee() {
        User user = currentUserProvider.getCurrentUser();
        return (user != null) ? user.getEmployee() : null;
    }
}