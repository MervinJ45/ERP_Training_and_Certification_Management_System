package com.example.view.mainview;

import com.example.entity.User;
import com.example.utils.CurrentUserProvider;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MainLayout extends AppLayout {

    private String manager = "ROLE_MANAGER";
    private String hr = "ROLE_HR";
    private String admin = "ROLE_ADMIN";
    private String auditor = "ROLE_AUDITOR";
    private String employee = "ROLE_EMPLOYEE";
    private String superAdmin = "ROLE_SUPER_ADMIN";
    private String director = "ROLE_DIRECTOR";
    private String trainer = "ROLE_TRAINER";
    private final AuthenticationContext authenticationContext;
    private final CurrentUserProvider currentUserProvider;

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String role = auth.getAuthorities().iterator().next().getAuthority();

    public MainLayout(AuthenticationContext authenticationContext, CurrentUserProvider currentUserProvider) {
        this.authenticationContext = authenticationContext;
        this.currentUserProvider = currentUserProvider;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("ERP Training & Certification");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfo.setSpacing(true);

        User currentUser = currentUserProvider.getCurrentUser();

        Span name = new Span(currentUser.getUsername());
        name.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL);

        Span role = new Span(currentUser.getRole().getRoleName());
        role.addClassNames(LumoUtility.FontSize.XXSMALL, LumoUtility.TextColor.SECONDARY);
        role.getStyle().set("text-transform", "uppercase");

        VerticalLayout nameAndRole = new VerticalLayout(name, role);
        nameAndRole.setSpacing(false);
        nameAndRole.setPadding(false);
        nameAndRole.setAlignItems(FlexComponent.Alignment.END);

        userInfo.add(nameAndRole);

        Button logout = new Button("Logout", e -> {
            authenticationContext.logout();
        });

        HorizontalLayout header = new HorizontalLayout(toggle, logo, userInfo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", "super-admin-dashboard", VaadinIcon.DASHBOARD.create()));


        if (role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Employees", "employee", VaadinIcon.USER_CARD.create()));
            nav.addItem(new SideNavItem("Users", "user", VaadinIcon.USER.create()));
        }
        if (role.equals(employee) || role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Course Catalog", "/courses", VaadinIcon.ACADEMY_CAP.create()));
            nav.addItem(new SideNavItem("My Enrollments", "/my-enrollments", VaadinIcon.LIST.create()));
            nav.addItem(new SideNavItem("Certifications", "/my-certificates", VaadinIcon.CART.create()));
        }
        if (role.equals(hr) || role.equals(admin) || role.equals(auditor)) {
            nav.addItem(new SideNavItem("Manage Courses", "/manage-courses", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("Pending Approvals", "/approvals", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("Employee Skill Matrix", "/skill-matrix-summary", VaadinIcon.CHART_3D.create()));
        }
        if (role.equals(director)) {
            nav.addItem(new SideNavItem("Pending Approvals", "/approvals", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("Certificate Renewal Request", "/cert-approvals", VaadinIcon.EDIT.create()));
        }
        if (role.equals(manager) || role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Pending Approvals", "/manager-approvals", VaadinIcon.CHECK_SQUARE_O.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.CHECK.create()));
        }
        if (role.equals(auditor) || role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Reports", "/reports", VaadinIcon.BAR_CHART.create()));
            nav.addItem(new SideNavItem("Audit Timeline", "/audit", VaadinIcon.CLIPBOARD_TEXT.create()));
        }
        if (role.equals(trainer)) {
            nav.addItem(new SideNavItem("My-Class Enrollments", "/trainer-enrollments", VaadinIcon.BAR_CHART.create()));
        }
        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }
}