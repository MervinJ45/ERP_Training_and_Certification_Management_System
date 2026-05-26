package com.example.view.mainview;

import com.example.entity.User;
import com.example.service.UserService;
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

    private final String manager = "ROLE_MANAGER";
    private final String hr = "ROLE_HR";
    private final String admin = "ROLE_ADMIN";
    private final String auditor = "ROLE_AUDITOR";
    private final String employee = "ROLE_EMPLOYEE";
    private final String superAdmin = "ROLE_SUPER_ADMIN";
    private final String director = "ROLE_DIRECTOR";
    private final String trainer = "ROLE_TRAINER";

    private final AuthenticationContext authenticationContext;
    private final CurrentUserProvider currentUserProvider;
    private final UserService userService;

    private final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    private final String role = (auth != null && !auth.getAuthorities().isEmpty()) ? auth.getAuthorities().iterator().next().getAuthority() : "";

    public MainLayout(AuthenticationContext authenticationContext, CurrentUserProvider currentUserProvider, UserService userService) {
        this.authenticationContext = authenticationContext;
        this.currentUserProvider = currentUserProvider;
        this.userService = userService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("ERP Training & Certification");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM, LumoUtility.TextColor.HEADER);

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfo.setSpacing(true);

        User currentUser = currentUserProvider.getCurrentUser();

        String displayName = "";
        if (currentUser != null) {
            if (currentUser.getEmployee() != null) {
                String firstName = currentUser.getEmployee().getFirstName();
                String lastName = currentUser.getEmployee().getLastName();
                displayName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();

                if (displayName.isEmpty()) {
                    displayName = currentUser.getUsername();
                }
            } else {
                displayName = currentUser.getUsername();
            }
        }

        Button nameClickBtn = new Button(displayName);
        nameClickBtn.getElement().getThemeList().add("tertiary");
        nameClickBtn.getStyle().set("padding", "0");
        nameClickBtn.getStyle().set("margin", "0");
        nameClickBtn.getStyle().set("height", "auto");
        nameClickBtn.getStyle().set("line-height", "1.2");
        nameClickBtn.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.BODY);

        if (currentUser != null) {
            final Long targetId = currentUser.getUserId();
            nameClickBtn.addClickListener(e -> new ChangePasswordDialog(userService, targetId).open());
        }

        Span roleBadge = new Span(currentUser != null && currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "GUEST");
        roleBadge.getElement().getThemeList().add("badge contrast small");
        roleBadge.addClassNames(LumoUtility.FontSize.XXSMALL, LumoUtility.FontWeight.SEMIBOLD);
        roleBadge.getStyle().set("text-transform", "uppercase");

        VerticalLayout nameAndRole = new VerticalLayout(nameClickBtn, roleBadge);
        nameAndRole.setSpacing(false);
        nameAndRole.setPadding(false);
        nameAndRole.setAlignItems(FlexComponent.Alignment.END);

        userInfo.add(nameAndRole);

        Button logout = new Button("Logout", VaadinIcon.SIGN_OUT.create(), e -> authenticationContext.logout());
        logout.getElement().getThemeList().add("tertiary error");

        HorizontalLayout header = new HorizontalLayout(toggle, logo, userInfo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

        nav.addItem(new SideNavItem("Dashboard", "", VaadinIcon.DASHBOARD.create()));

        if (role.equals(superAdmin)) {
            nav.addItem(new SideNavItem("Employees", "/employee", VaadinIcon.USER_CARD.create()));
            nav.addItem(new SideNavItem("Courses", "/training-courses", VaadinIcon.ACADEMY_CAP.create()));
            nav.addItem(new SideNavItem("Enrollments", "/training-enrollments", VaadinIcon.CLIPBOARD_USER.create()));
            nav.addItem(new SideNavItem("Certifications", "/certifications", VaadinIcon.DIPLOMA.create()));
        }

        if (role.equals(employee)) {
            nav.addItem(new SideNavItem("Course Catalog", "/courses", VaadinIcon.ACADEMY_CAP.create()));
            nav.addItem(new SideNavItem("My Enrollments", "/my-enrollments", VaadinIcon.CLIPBOARD_USER.create()));
            nav.addItem(new SideNavItem("My Certificates", "/my-certificates", VaadinIcon.DIPLOMA.create()));
            nav.addItem(new SideNavItem("Certificate History", "/certificate-history", VaadinIcon.DIPLOMA.create()));
        }

        if (role.equals(hr) || role.equals(admin)) {
            nav.addItem(new SideNavItem("Manage Courses", "/manage-courses", VaadinIcon.RECORDS.create()));
            nav.addItem(new SideNavItem("Pending Approvals", "/approvals", VaadinIcon.CLOCK.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.CHECK_SQUARE_O.create()));
            nav.addItem(new SideNavItem("Employee Skill Matrix", "/skill-matrix-summary", VaadinIcon.GRID_BIG_O.create()));
        }

        if (role.equals(director)) {
            nav.addItem(new SideNavItem("Pending Approvals", "/approvals", VaadinIcon.BELL.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.CHECK_SQUARE_O.create()));
        }

        if (role.equals(manager)) {
            nav.addItem(new SideNavItem("Pending Approvals", "/manager-approvals", VaadinIcon.CLOCK.create()));
            nav.addItem(new SideNavItem("My Approvals", "/my-approvals", VaadinIcon.CHECK.create()));
            nav.addItem(new SideNavItem("Certificate Renewal Request", "/cert-approvals", VaadinIcon.REFRESH.create()));
        }

        if (role.equals(trainer)) {
            nav.addItem(new SideNavItem("My-Class Enrollments", "/trainer-enrollments", VaadinIcon.PRESENTATION.create()));
        }

        if (role.equals(auditor)) {
            nav.addItem(new SideNavItem("Employee Training", "/training-enrollments", VaadinIcon.USER_CARD.create()));
            nav.addItem(new SideNavItem("Certification Expiry", "reports/certification-expiry", VaadinIcon.DIPLOMA_SCROLL.create()));
            nav.addItem(new SideNavItem("Training Cost Analysis", "reports/training-cost", VaadinIcon.MONEY.create()));
            nav.addItem(new SideNavItem("Enrollment Summaries", "reports/enrollment-summary", VaadinIcon.CLIPBOARD_CHECK.create()));
            nav.addItem(new SideNavItem("Pending Verification", "reports/pending-approvals", VaadinIcon.CLOCK.create()));
            nav.addItem(new SideNavItem("Audit Ledger Logs", "reports/audit-history", VaadinIcon.FILE_TEXT.create()));
        }

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }
}