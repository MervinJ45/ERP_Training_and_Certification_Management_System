package com.example.view.superadminview;

import com.example.entity.Certification;
import com.example.entity.TrainingApproval;
import com.example.entity.TrainingEnrollment;
import com.example.service.DashboardService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Route(value = "super-admin-dashboard", layout = MainLayout.class)
@PageTitle("Super Admin Dashboard")
@AnonymousAllowed
public class SuperAdminDashboardView extends VerticalLayout {

    private final DashboardService dashboardService;

    public SuperAdminDashboardView(DashboardService dashboardService) {

        this.dashboardService = dashboardService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Super Admin Dashboard");
        add(title);

        HorizontalLayout kpiLayout1 = new HorizontalLayout();
        kpiLayout1.setWidthFull();

        kpiLayout1.add(createKpiCard("Total Employees", String.valueOf(dashboardService.getTotalEmployees()), "👨‍💼"), createKpiCard("Departments", String.valueOf(dashboardService.getTotalDepartments()), "🏢"), createKpiCard("Courses", String.valueOf(dashboardService.getTotalCourses()), "📘"), createKpiCard("Enrollments", String.valueOf(dashboardService.getTotalEnrollments()), "📝"));

        HorizontalLayout kpiLayout2 = new HorizontalLayout();
        kpiLayout2.setWidthFull();

        kpiLayout2.add(createKpiCard("Certifications", String.valueOf(dashboardService.getTotalCertifications()), "🎓"), createKpiCard("Pending Approvals", String.valueOf(dashboardService.getPendingApprovals().size()), "⏳"), createKpiCard("Expiring Certifications", String.valueOf(dashboardService.getExpiringCertifications().size()), "⚠️"), createKpiCard("Training Cost", "₹ " + dashboardService.getTotalTrainingCost(), "💰"));
        add(kpiLayout1, kpiLayout2);
    }

    private VerticalLayout createKpiCard(String title, String value, String emoji) {

        Span icon = new Span(emoji);
        icon.getStyle().set("font-size", "30px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "14px").set("color", "gray");

        H3 valueText = new H3(value);

        VerticalLayout card = new VerticalLayout(icon, titleSpan, valueText);
        card.setWidth("250px");
        card.getStyle().set("border-radius", "15px").set("padding", "20px").set("background-color", "white").set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        return card;
    }
}