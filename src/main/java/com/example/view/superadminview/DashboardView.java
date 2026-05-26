package com.example.view.superadminview;

import com.example.dto.*;
import com.example.service.*;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("ERP | Dashboard")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    private final TrainingCourseService courseService;
    private final TrainingEnrollmentService enrollmentService;
    private final CertificationService certificationService;
    private final DepartmentService departmentService;
    private final SkillMatrixService skillMatrixService;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DashboardView(TrainingCourseService courseService, TrainingEnrollmentService enrollmentService, CertificationService certificationService, DepartmentService departmentService, SkillMatrixService skillMatrixService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.certificationService = certificationService;
        this.departmentService = departmentService;
        this.skillMatrixService = skillMatrixService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        addClassName(LumoUtility.Background.CONTRAST_5);

        H2 header = new H2("Training Management Dashboard");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.NONE);
        add(header);

        List<TrainingCourseDTO> allCourses = courseService.getAllCourseDTOs();
        List<TrainingEnrollmentDTO> allEnrollments = enrollmentService.getAllEnrollmentDTOs();
        List<CertificationDisplayDTO> allCertifications = certificationService.getAllCertificationDTOs();

        List<CertificationDisplayDTO> expiringOrExpired = allCertifications.stream().filter(c -> c.getStatusName() != null && !"Renewed".equalsIgnoreCase(c.getStatusName())).filter(c -> c.getDaysRemaining() != null && c.getDaysRemaining() <= 30).toList();

        long totalActivePrograms = allCourses.stream().filter(TrainingCourseDTO::isActive).count();
        long activeEnrollments = allEnrollments.stream().filter(e -> "Approved".equalsIgnoreCase(e.getEnrollmentStatusName())).count();
        long pendingApprovals = allEnrollments.stream().filter(e -> "Pending Approval".equalsIgnoreCase(e.getEnrollmentStatusName())).count();
        long expiringCertifications = expiringOrExpired.size();

        HorizontalLayout kpiRow = new HorizontalLayout();
        kpiRow.setWidthFull();

        Component totalProgramsCard = createKpiCard("Total Programs", String.valueOf(totalActivePrograms), VaadinIcon.ACADEMY_CAP, "blue");
        Component activeEnrollmentsCard = createKpiCard("Active Enrollments", String.valueOf(activeEnrollments), VaadinIcon.USER_CHECK, "green");
        Component pendingApprovalsCard = createKpiCard("Pending Approvals", String.valueOf(pendingApprovals), VaadinIcon.CLOCK, "orange");
        Component upcomingExpiryCard = createKpiCard("Expiring Certifications", String.valueOf(expiringCertifications), VaadinIcon.WARNING, "red");

        kpiRow.add(totalProgramsCard, activeEnrollmentsCard, pendingApprovalsCard, upcomingExpiryCard);
        add(kpiRow);

        HorizontalLayout middleRow = new HorizontalLayout();
        middleRow.setWidthFull();
        middleRow.setHeight("400px");

        Component deptSummary = createDepartmentSummaryWidget();
        Component expiryGrid = createExpiryDetailsWidget(expiringOrExpired);

        middleRow.add(deptSummary, expiryGrid);
        add(middleRow);

        Component skillSummary = createSkillSummaryWidget();
        add(skillSummary);
    }

    private Component createKpiCard(String title, String value, VaadinIcon iconType, String colorTheme) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10, LumoUtility.Padding.MEDIUM);

        Icon icon = iconType.create();
        icon.setSize("2.5rem");

        switch (colorTheme) {
            case "blue" -> icon.addClassName(LumoUtility.TextColor.PRIMARY);
            case "green" -> icon.addClassName(LumoUtility.TextColor.SUCCESS);
            case "orange" -> icon.addClassName(LumoUtility.TextColor.WARNING);
            case "red" -> icon.addClassName(LumoUtility.TextColor.ERROR);
        }

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        Span labelSpan = new Span(title);
        labelSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

        textLayout.add(labelSpan, valueSpan);
        card.add(icon, textLayout);
        card.setFlexGrow(1, textLayout);

        return card;
    }

    private Component createDepartmentSummaryWidget() {
        VerticalLayout container = createSectionContainer("Department Budget");

        Grid<DepartmentDTO> grid = new Grid<>(DepartmentDTO.class, false);
        grid.addColumn(DepartmentDTO::getDepartmentName).setHeader("Department").setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(DepartmentDTO::getAnnualBudget, currencyFormat)).setHeader("Annual Budget").setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(DepartmentDTO::getAvailableBalance, currencyFormat)).setHeader("Available Balance").setAutoWidth(true);

        if (departmentService != null) {
            grid.setItems(departmentService.getAllDepartmentDTOs());
        }

        container.add(grid);
        return container;
    }

    private Component createExpiryDetailsWidget(List<CertificationDisplayDTO> expiringOrExpired) {
        VerticalLayout container = createSectionContainer("Upcoming Certification Expiries");

        Grid<CertificationDisplayDTO> grid = new Grid<>(CertificationDisplayDTO.class, false);
        grid.addColumn(CertificationDisplayDTO::getEmployee).setHeader("Employee").setAutoWidth(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Course").setAutoWidth(true);

        grid.addColumn(cert -> {
            if (cert.getDaysRemaining() == null) return "N/A";
            return cert.getDaysRemaining() > 0 ? cert.getDaysRemaining() + " Days" : "Expired";
        }).setHeader("Remaining").setAutoWidth(true);

        grid.addComponentColumn(cert -> {
            Span badge = new Span();
            String status = cert.getStatusName() != null ? cert.getStatusName() : "Unknown";
            badge.setText(status);
            badge.getElement().getThemeList().add("badge");

            switch (status.toUpperCase()) {
                case "EXPIRED" -> badge.getElement().getThemeList().add("error");
                case "EXPIRING SOON" -> badge.getElement().getThemeList().add("warning");
                default -> badge.getElement().getThemeList().add("success");
            }
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        grid.setItems(expiringOrExpired);

        container.add(grid);
        return container;
    }

    private Component createSkillSummaryWidget() {
        VerticalLayout container = createSectionContainer("Employee Skill Summary Matrices");
        container.setHeight("350px");

        Grid<SkillSummaryDTO> grid = new Grid<>(SkillSummaryDTO.class, false);
        grid.addColumn(SkillSummaryDTO::getEmployeeFullName).setHeader("Employee").setAutoWidth(true).setSortable(true);
        grid.addColumn(SkillSummaryDTO::getSkillName).setHeader("Acquired Skill").setAutoWidth(true).setSortable(true);
        grid.addColumn(SkillSummaryDTO::getCourseName).setHeader("Source Course").setAutoWidth(true);

        grid.addColumn(skill -> "★ ".repeat(Math.max(0, skill.getProficiencyRating())) + "☆ ".repeat(Math.max(0, 5 - skill.getProficiencyRating()))).setHeader("Proficiency Rating").setAutoWidth(true);

        if (skillMatrixService != null) {
            grid.setItems(skillMatrixService.getSkillMatrixSummary());
        }

        container.add(grid);
        return container;
    }

    private VerticalLayout createSectionContainer(String titleText) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        Span title = new Span(titleText);
        title.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.BODY);

        layout.add(title);
        return layout;
    }
}