package com.example.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

public class MainLayout extends AppLayout {

    private String manager = "ROLE_MANAGER";
    private String hr = "ROLE_HR";
    private String admin = "ROLE_ADMIN";
    private String auditor = "ROLE_AUDITOR";
    private String employee = "ROLE_EMPLOYEE";
    private String superAdmin = "ROLE_SUPERADMIN";

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String role = auth.getAuthorities().iterator().next().getAuthority();

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("ERP Training & Certification");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);

        DrawerToggle toggle = new DrawerToggle();

        Button logout = new Button("Logout", e -> {
            SecurityContextHolder.clearContext();
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().getPage().setLocation("/login");
            UI.getCurrent().close();
        });

        HorizontalLayout header = new HorizontalLayout(toggle, logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        if(role.equals(employee)) {
            nav.addItem(new SideNavItem("Course Catalog", "/courses", VaadinIcon.ACADEMY_CAP.create()));
            nav.addItem(new SideNavItem("My Enrollments", "/my-enrollments", VaadinIcon.LIST.create()));
            nav.addItem(new SideNavItem("Certifications", "/my-certifications", VaadinIcon.CART.create()));
        }
        if(role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Employees", "employee", VaadinIcon.USER_CARD.create()));
            nav.addItem(new SideNavItem("Users", "user", VaadinIcon.USER.create()));
        }

        if(role.equals(manager)) {
            nav.addItem(new SideNavItem("Pending Approvals", "/approvals", VaadinIcon.CHECK_SQUARE_O.create()));
        }
        if(role.equals(hr) || role.equals(admin)) {
            nav.addItem(new SideNavItem("Manage Courses", "/manage-courses", VaadinIcon.EDIT.create()));
            nav.addItem(new SideNavItem("Employee Skill Matrix", "/skill-matrix", VaadinIcon.CHART_3D.create()));
        }
        if(role.equals(auditor) || role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Reports", "/reports", VaadinIcon.BAR_CHART.create()));
            nav.addItem(new SideNavItem("Audit Timeline", "/audit", VaadinIcon.CLIPBOARD_TEXT.create()));
        }
        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }
}