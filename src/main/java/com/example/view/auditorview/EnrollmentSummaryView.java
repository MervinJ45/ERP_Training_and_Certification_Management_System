package com.example.view.auditorview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.TrainingApproval;
import com.example.service.TrainingApprovalService;
import com.example.service.TrainingEnrollmentService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("ERP | Enrollment Summary")
@Route(value = "reports/enrollment-summary", layout = MainLayout.class)
@RolesAllowed("AUDITOR")
public class EnrollmentSummaryView extends VerticalLayout {

    private final TrainingApprovalService approvalService;
    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>();

    public EnrollmentSummaryView(TrainingEnrollmentService enrollmentService, TrainingApprovalService approvalService) {
        this.approvalService = approvalService;

        setSizeFull();
        setPadding(true);

        H2 header = new H2("Enrollment Summary Ledger");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.HEADER);
        add(header);

        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentId).setHeader("Ref ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course Requested").setAutoWidth(true).setSortable(true);

        grid.addColumn(dto -> dto.getEnrollmentDate() != null ? dto.getEnrollmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").setHeader("Submission Date").setAutoWidth(true).setSortable(true);

        grid.addComponentColumn(this::createEnrollmentStatusBadge).setHeader("Current State Summary").setAutoWidth(true).setSortable(true);

        grid.setDetailsVisibleOnClick(true);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createApprovalDropdownLedgerContent));

        grid.setItems(enrollmentService.getAllEnrollmentDTOs());

        add(grid);
        setFlexGrow(1, grid);
    }

    private VerticalLayout createApprovalDropdownLedgerContent(TrainingEnrollmentDTO enrollment) {
        VerticalLayout dropdownContainer = new VerticalLayout();
        dropdownContainer.setPadding(true);
        dropdownContainer.setSpacing(true);

        dropdownContainer.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-left", "4px solid var(--lumo-primary-color)").set("margin", "4px 0px");

        HorizontalLayout dropdownHeader = new HorizontalLayout();
        dropdownHeader.setAlignItems(Alignment.CENTER);
        var trailIcon = VaadinIcon.FILE_TREE.create();
        trailIcon.setSize("16px");
        trailIcon.addClassName(LumoUtility.TextColor.PRIMARY);

        Span textTitle = new Span(" Approval records for this enrollment:");
        textTitle.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.HEADER);
        dropdownHeader.add(trailIcon, textTitle);
        dropdownContainer.add(dropdownHeader);

        List<TrainingApproval> approvalHistory = approvalService.getApprovalsByEnrollmentId(enrollment.getEnrollmentId());

        if (approvalHistory.isEmpty()) {
            Span noApprovalsMsg = new Span("No Approvals for this record yet.");
            noApprovalsMsg.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
            noApprovalsMsg.getStyle().set("font-style", "italic").set("padding-left", "24px");
            dropdownContainer.add(noApprovalsMsg);
        } else {
            Grid<TrainingApproval> nestedApprovalGrid = new Grid<>();
            nestedApprovalGrid.setAllRowsVisible(true);
            nestedApprovalGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
            nestedApprovalGrid.setItems(approvalHistory);

            nestedApprovalGrid.addColumn(TrainingApproval::getApprovalLevel).setHeader("Workflow Gate Level").setAutoWidth(true);

            nestedApprovalGrid.addColumn(approval -> approval.getApprover() != null ? approval.getApprover().getFirstName() + " " + approval.getApprover().getLastName() : "SYSTEM").setHeader("Verified By").setAutoWidth(true);

            nestedApprovalGrid.addColumn(approval -> approval.getActionDate() != null ? approval.getActionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").setHeader("Execution Time").setAutoWidth(true);

            nestedApprovalGrid.addComponentColumn(this::createApprovalStatusBadge).setHeader("Gate Verdict").setAutoWidth(true);

            nestedApprovalGrid.addColumn(TrainingApproval::getComments).setHeader("Remarks / Statements").setAutoWidth(true);

            dropdownContainer.add(nestedApprovalGrid);
        }

        return dropdownContainer;
    }

    private Span createEnrollmentStatusBadge(TrainingEnrollmentDTO enrollment) {
        String status = enrollment.getEnrollmentStatusName() != null ? enrollment.getEnrollmentStatusName() : "Pending";
        Span badge = new Span(status);
        badge.getElement().getThemeList().add("badge");

        String style = switch (status) {
            case "Certified", "Completed" ->
                    "background-color: var(--lumo-success-color-10pct); color: var(--lumo-success-text-color); font-weight: 600;";
            case "Approved" ->
                    "background-color: var(--lumo-primary-color-10pct); color: var(--lumo-primary-text-color); font-weight: 600;";
            case "Rejected" ->
                    "background-color: var(--lumo-error-color-10pct); color: var(--lumo-error-text-color); font-weight: 600;";
            default ->
                    "background-color: var(--lumo-warning-color-10pct); color: var(--lumo-warning-text-color); font-weight: 600;";
        };
        badge.getElement().setAttribute("style", style);
        return badge;
    }

    private Span createApprovalStatusBadge(TrainingApproval approval) {
        String status = (approval.getApprovalStatus() != null) ? approval.getApprovalStatus().getApprovalStatus() : "Approved";
        Span badge = new Span(status);
        badge.getElement().getThemeList().add("badge small");

        String style = "Approved".equalsIgnoreCase(status) ? "background-color: #DCFCE7; color: #15803D; font-weight: bold;" : "background-color: #FEE2E2; color: #B91C1C; font-weight: bold;";

        badge.getElement().setAttribute("style", style);
        return badge;
    }
}