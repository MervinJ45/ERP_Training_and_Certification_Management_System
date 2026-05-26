package com.example.view.hrview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.User;
import com.example.service.TrainingEnrollmentService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "approvals", layout = MainLayout.class)
@PageTitle("ERP | Pending Approvals ")
@RolesAllowed({"MANAGER", "HR", "DIRECTOR", "ADMIN"})
public class HrOrDirectorApprovalView extends VerticalLayout {

    private final TrainingEnrollmentService trainingEnrollmentService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    private final TextField employeeSearchField = new TextField();
    private final TextField courseSearchField = new TextField();

    private List<TrainingEnrollmentDTO> allData;
    private final NumberFormat inrFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public HrOrDirectorApprovalView(TrainingEnrollmentService trainingEnrollmentService, CurrentUserProvider currentUserProvider) {
        this.trainingEnrollmentService = trainingEnrollmentService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Pending Approvals");

        configureEmployeeSearch();
        configureCourseSearch();
        configureGrid();

        HorizontalLayout filterActionLayout = new HorizontalLayout(employeeSearchField, courseSearchField);
        filterActionLayout.setSpacing(true);
        filterActionLayout.setWidthFull();

        grid.setSizeFull();

        add(title, filterActionLayout, grid);
        loadData();
    }

    private void configureEmployeeSearch() {
        employeeSearchField.setPlaceholder("Search by Employee");
        employeeSearchField.setClearButtonVisible(true);
        employeeSearchField.setPrefixComponent(VaadinIcon.USER.create());
        employeeSearchField.setWidth("240px");
        employeeSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        employeeSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureCourseSearch() {
        courseSearchField.setPlaceholder("Search by Course");
        courseSearchField.setClearButtonVisible(true);
        courseSearchField.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());
        courseSearchField.setWidth("240px");
        courseSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        courseSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getDepartmentName).setHeader("Department").setAutoWidth(true).setSortable(true);

        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course").setAutoWidth(true).setSortable(true);

        grid.addColumn(dto -> dto.getRequestedCost() != null ? inrFormatter.format(dto.getRequestedCost()) : "₹0.00").setHeader("Requested Cost").setAutoWidth(true).setSortable(true);

        grid.addColumn(dto -> dto.getAvailableBalance() != null ? inrFormatter.format(dto.getAvailableBalance()) : "₹0.00").setHeader("Available Balance").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons)).setHeader("Actions").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private Component createActionButtons(TrainingEnrollmentDTO dto) {
        Button approveBtn = new Button("Approve", VaadinIcon.CHECK.create());
        approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        approveBtn.addClickListener(e -> openApprovalDialog(dto));

        Button rejectBtn = new Button("Reject", VaadinIcon.CLOSE.create());
        rejectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        rejectBtn.addClickListener(e -> openRejectionDialog(dto));

        HorizontalLayout actionsLayout = new HorizontalLayout(approveBtn, rejectBtn);
        actionsLayout.setSpacing(true);
        return actionsLayout;
    }

    private void filterGrid() {
        if (allData == null) return;

        String employeeQuery = employeeSearchField.getValue() != null ? employeeSearchField.getValue().trim().toLowerCase() : "";
        String courseQuery = courseSearchField.getValue() != null ? courseSearchField.getValue().trim().toLowerCase() : "";

        if (employeeQuery.isEmpty() && courseQuery.isEmpty()) {
            grid.setItems(allData);
            return;
        }

        List<TrainingEnrollmentDTO> filteredList = allData.stream().filter(dto -> {
            boolean matchesEmployee = employeeQuery.isEmpty() || (dto.getEmployeeFullName() != null && dto.getEmployeeFullName().toLowerCase().contains(employeeQuery));
            boolean matchesCourse = courseQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));
            return matchesEmployee && matchesCourse;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }

    private void openApprovalDialog(TrainingEnrollmentDTO dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Approve Enrollment");

        NumberField approvedCost = new NumberField("Approved Cost");
        approvedCost.setValue(dto.getRequestedCost().doubleValue());
        approvedCost.setPrefixComponent(new Span("₹"));
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

    public void loadData() {
        User user = currentUserProvider.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            Notification.show("Session Error: Employee entity missing.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Long roleId = user.getEmployee().getUser().getRole().getRoleId();
        allData = trainingEnrollmentService.getHrOrDirectorApprovals(roleId);
        filterGrid();
    }

    private Long getCurrentEmployeeId() {
        User user = currentUserProvider.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            return user.getEmployee().getEmployeeId();
        }
        throw new IllegalStateException("No valid employee link found for current user session.");
    }
}