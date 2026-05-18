package com.example.view.hrview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.User;
import com.example.service.TrainingEnrollmentService;
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
import java.util.List;

@Route(value = "approvals", layout = MainLayout.class)
@PageTitle("ERP | Pending Approvals ")
@RolesAllowed({"MANAGER", "HR", "DIRECTOR", "ADMIN"})
public class HrOrDirectorApprovalView extends VerticalLayout {

    private final TrainingEnrollmentService trainingEnrollmentService;
    private final AuthenticationContext authContext;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public HrOrDirectorApprovalView(TrainingEnrollmentService trainingEnrollmentService, AuthenticationContext authContext, CurrentUserProvider currentUserProvider) {
        this.trainingEnrollmentService = trainingEnrollmentService;
        this.authContext = authContext;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();

        H2 title = new H2("Pending Approvals Queue");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {
        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getRequestedCost).setHeader("Requested Cost").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(dto -> {
            Button approveBtn = new Button("Approve");
            approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            approveBtn.addClickListener(e -> openApprovalDialog(dto));
            return approveBtn;
        }).setHeader("Approve").setAutoWidth(true);

        grid.addComponentColumn(dto -> {
            Button rejectBtn = new Button("Reject");
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            rejectBtn.addClickListener(e -> openRejectionDialog(dto));
            return rejectBtn;
        }).setHeader("Reject").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private void openApprovalDialog(TrainingEnrollmentDTO dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Approve Enrollment");

        NumberField approvedCost = new NumberField("Approved Cost");
        approvedCost.setValue(dto.getRequestedCost().doubleValue());
        approvedCost.setWidthFull();

        TextArea comments = new TextArea("Approval Comments");
        comments.setPlaceholder("Enter operational context remarks here...");
        comments.setHeight("8em");
        comments.setWidthFull();

        Button approveBtn = new Button("Confirm Approval", e -> {
            BigDecimal finalCost = (approvedCost.getValue() != null) ? BigDecimal.valueOf(approvedCost.getValue()) : dto.getRequestedCost();

            try {
                Long approverId = getCurrentEmployeeId();
                trainingEnrollmentService.approveEnrollment(dto.getEnrollmentId(), approverId, finalCost, comments.getValue());

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
        comments.setPlaceholder("Specify details regarding budget limits or scheduling conflicts...");
        comments.setHeight("8em");
        comments.setWidthFull();
        comments.setRequired(true);

        Button confirmRejectBtn = new Button("Reject Request", e -> {
            if (comments.getValue().trim().isEmpty()) {
                comments.setInvalid(true);
                comments.setErrorMessage("Rejection comments are mandatory for corporate tracking.");
                return;
            }

            try {
                Long approverId = getCurrentEmployeeId();
                trainingEnrollmentService.rejectEnrollment(dto.getEnrollmentId(), approverId, comments.getValue());

                Notification n = Notification.show("Enrollment Request Turned Down");
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
        User user = currentUserProvider.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            Notification.show("Session Error: Employee entity missing.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Long roleId = user.getEmployee().getUser().getRole().getRoleId();

        List<TrainingEnrollmentDTO> pendingApprovals = trainingEnrollmentService.getHrOrDirectorApprovals(roleId);
        grid.setItems(pendingApprovals);
    }

    private Long getCurrentEmployeeId() {
        User user = currentUserProvider.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            return user.getEmployee().getEmployeeId();
        }
        throw new IllegalStateException("No valid employee link found for current user session.");
    }
}