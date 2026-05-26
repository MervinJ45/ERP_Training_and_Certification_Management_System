package com.example.view.employeeview;

import com.example.view.component.WorkflowInfoDialog;
import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.ApprovalWorkflowConfigService;
import com.example.service.TrainingEnrollmentService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "my-enrollments", layout = MainLayout.class)
@PageTitle("ERP | My Enrollments")
@RolesAllowed("EMPLOYEE")
public class MyEnrollmentView extends VerticalLayout {

    private final TrainingEnrollmentService enrollmentService;
    private final CurrentUserProvider currentUserProvider;
    private final ApprovalWorkflowConfigService workflowConfigService;

    private Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public MyEnrollmentView(TrainingEnrollmentService enrollmentService, CurrentUserProvider currentUserProvider, ApprovalWorkflowConfigService workflowConfigService) {
        this.enrollmentService = enrollmentService;
        this.currentUserProvider = currentUserProvider;
        this.workflowConfigService = workflowConfigService;

        setSizeFull();

        H2 title = new H2("My Enrollments");

        configureGrid();
        loadData();

        add(title, grid);
    }

    private void configureGrid() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course");
        grid.addColumn(dto -> dto.getEnrollmentDate() != null ? dto.getEnrollmentDate().toLocalDate() : "-").setHeader("Enrollment Date");
        grid.addColumn(new NumberRenderer<>(TrainingEnrollmentDTO::getRequestedCost, currencyFormat)).setHeader("Requested Cost");
        grid.addColumn(dto -> dto.getApprovedCost() != null ? currencyFormat.format(dto.getApprovedCost()) : "-").setHeader("Approved Cost");

        grid.addColumn(new ComponentRenderer<>(dto -> {
            Span badge = new Span();
            String status = dto.getEnrollmentStatusName() != null ? dto.getEnrollmentStatusName() : "Pending Approval";
            badge.setText(status);
            badge.getElement().getThemeList().add("badge");

            switch (status) {
                case "Approved":
                case "Certified":
                case "Completed":
                    badge.getElement().getThemeList().add("success");
                    break;
                case "Rejected":
                    badge.getElement().getThemeList().add("error");
                    break;
                case "Pending Approval":
                default:
                    badge.getElement().getThemeList().add("warning");
                    break;
            }
            return badge;
        })).setHeader("Status");

        grid.addColumn(dto -> {
            Integer currentLevel = dto.getCurrentApprovalLevel();
            long totalLevels = workflowConfigService.calculateTotalLevelsForCost(dto.getRequestedCost());

            if (currentLevel == null) {
                return "0 / " + totalLevels;
            }
            return currentLevel + " / " + totalLevels;
        }).setHeader(createApprovalLevelHeader());

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private HorizontalLayout createApprovalLevelHeader() {
        Span headerText = new Span("Approval Level");

        Button infoBtn = new Button(VaadinIcon.INFO_CIRCLE.create());
        infoBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        infoBtn.getStyle().set("cursor", "pointer");
        infoBtn.setTooltipText("View Approval Tiers");

        infoBtn.addClickListener(e -> new WorkflowInfoDialog(workflowConfigService).open());

        HorizontalLayout headerLayout = new HorizontalLayout(headerText, infoBtn);
        headerLayout.setSpacing(true);
        headerLayout.setVerticalComponentAlignment(Alignment.CENTER, headerText, infoBtn);

        return headerLayout;
    }

    private void loadData() {
        User user = currentUserProvider.getCurrentUser();
        Employee employee = user.getEmployee();

        grid.setItems(enrollmentService.getEnrollmentsByEmployee(employee.getEmployeeId()));
    }
}