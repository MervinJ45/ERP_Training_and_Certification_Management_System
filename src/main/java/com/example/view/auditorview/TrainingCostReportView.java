package com.example.view.auditorview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.service.TrainingEnrollmentService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("ERP | Training Cost Report")
@Route(value = "reports/training-cost", layout = MainLayout.class)
@RolesAllowed("AUDITOR")
public class TrainingCostReportView extends VerticalLayout {

    public TrainingCostReportView(TrainingEnrollmentService enrollmentService) {
        setSizeFull();
        setPadding(true);
        addClassName(LumoUtility.Background.CONTRAST_5);

        H2 header = new H2("Training Cost Report");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.XSMALL, LumoUtility.TextColor.HEADER);

        List<TrainingEnrollmentDTO> approvedEnrollments = enrollmentService.getApprovedEnrollmentDTOs();

        BigDecimal totalApprovedCost = approvedEnrollments.stream().map(TrainingEnrollmentDTO::getApprovedCost).filter(cost -> cost != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        HorizontalLayout summaryCard = new HorizontalLayout();
        summaryCard.setWidthFull();
        summaryCard.setPadding(true);
        summaryCard.setAlignItems(Alignment.CENTER);
        summaryCard.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10, LumoUtility.Margin.Bottom.MEDIUM);

        var walletIcon = VaadinIcon.WALLET.create();
        walletIcon.setSize("24px");
        walletIcon.addClassName(LumoUtility.TextColor.SUCCESS);

        VerticalLayout kpiLayout = new VerticalLayout();
        kpiLayout.setPadding(false);
        kpiLayout.setSpacing(false);

        Span kpiLabel = new Span("Total Approved Investment");
        kpiLabel.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);

        Span kpiValue = new Span(currencyFormat.format(totalApprovedCost));
        kpiValue.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.SUCCESS);

        kpiLayout.add(kpiLabel, kpiValue);
        summaryCard.add(walletIcon, kpiLayout);
        summaryCard.setFlexGrow(1, kpiLayout);

        Grid<TrainingEnrollmentDTO> grid = new Grid<>();
        grid.setSizeFull();
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course Source").setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(TrainingEnrollmentDTO::getRequestedCost, currencyFormat)).setHeader("Requested Cost").setAutoWidth(true).setSortable(true);

        grid.addColumn(new NumberRenderer<>(TrainingEnrollmentDTO::getApprovedCost, currencyFormat)).setHeader("Approved Final Cost").setAutoWidth(true).setSortable(true);

        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Course Source").setAutoWidth(true);

        grid.setItems(approvedEnrollments);
        add(header, summaryCard, grid);
        setFlexGrow(1, grid);
    }
}