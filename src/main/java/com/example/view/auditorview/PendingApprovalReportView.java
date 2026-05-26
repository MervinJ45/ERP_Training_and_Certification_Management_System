package com.example.view.auditorview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.service.TrainingEnrollmentService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("ERP | Pending Approval Report")
@Route(value = "reports/pending-approvals", layout = MainLayout.class)
@RolesAllowed("AUDITOR")
public class PendingApprovalReportView extends VerticalLayout {

    public PendingApprovalReportView(TrainingEnrollmentService enrollmentService) {
        setSizeFull();
        setPadding(true);

        H2 header = new H2("Pending Approval Report");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        add(header);

        Grid<TrainingEnrollmentDTO> grid = new Grid<>();
        grid.setSizeFull();
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Requester").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Target Course").setAutoWidth(true);
        grid.addColumn(TrainingEnrollmentDTO::getCurrentApprovalLevel).setHeader("Level Gate").setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(
                TrainingEnrollmentDTO::getRequestedCost,
                NumberFormat.getCurrencyInstance(new Locale("en", "IN"))
        )).setHeader("Budget Impact").setAutoWidth(true);

        List<TrainingEnrollmentDTO> items = enrollmentService.getAllEnrollmentDTOs().stream()
                .filter(e -> "Pending Approval".equalsIgnoreCase(e.getEnrollmentStatusName()))
                .toList();
        grid.setItems(items);

        add(grid);
        setFlexGrow(1, grid);
    }
}