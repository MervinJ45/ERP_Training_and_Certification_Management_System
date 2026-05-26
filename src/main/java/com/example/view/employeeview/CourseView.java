package com.example.view.employeeview;

import com.example.dto.TrainingCategoryDTO;
import com.example.dto.TrainingCourseDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.*;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "courses", layout = MainLayout.class)
@PageTitle("ERP | Courses")
@RolesAllowed({"EMPLOYEE", "SUPER_ADMIN"})
public class CourseView extends VerticalLayout {

    private final AuthenticationContext authContext;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final TrainingEnrollmentService trainingEnrollmentService;
    private final TrainingCourseService trainingCourseService;
    private final CurrentUserProvider currentUserProvider;
    private Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);
    private final TextField courseSearchField = new TextField();
    private final ComboBox<TrainingCategoryDTO> categoryFilterField = new ComboBox<>();
    private List<TrainingCourseDTO> allCourses;
    private final TrainingCategoryService trainingCategoryService;

    public CourseView(TrainingCourseService trainingCourseService, AuthenticationContext authContext, UserService userService, EmployeeService employeeService, TrainingEnrollmentService trainingEnrollmentService, CurrentUserProvider currentUserProvider, TrainingCategoryService trainingCategoryService) {
        this.trainingCourseService = trainingCourseService;
        this.authContext = authContext;
        this.userService = userService;
        this.employeeService = employeeService;
        this.trainingEnrollmentService = trainingEnrollmentService;
        this.currentUserProvider = currentUserProvider;
        this.trainingCategoryService = trainingCategoryService;

        addClassName("course-view");
        setSizeFull();

        H2 title = new H2("Available Training Courses");
        title.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.XSMALL);

        configureGrid();
        configureFilters();

        HorizontalLayout filterLayout = new HorizontalLayout(courseSearchField, categoryFilterField);

        add(title, filterLayout, grid);
        updateGrid();
    }

    private void configureFilters() {
        courseSearchField.setPlaceholder("Search Course Name...");
        courseSearchField.setClearButtonVisible(true);
        courseSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        courseSearchField.setWidth("220px");
        courseSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        courseSearchField.addValueChangeListener(e -> filterGrid());

        categoryFilterField.setPlaceholder("Category (All)");
        categoryFilterField.setClearButtonVisible(true);
        categoryFilterField.setWidth("220px");
        categoryFilterField.setItems(trainingCategoryService.getAllCategoryDTOs());
        categoryFilterField.setItemLabelGenerator(item -> item != null ? item.getCategoryName() : "");
        categoryFilterField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.addClassNames("course-grid");
        grid.setSizeFull();

        grid.addColumn(TrainingCourseDTO::getCourseName).setHeader("Course Name").setSortable(true).setResizable(true);
        grid.addColumn(TrainingCourseDTO::getDurationDays).setHeader("Days").setAutoWidth(true);
        grid.addColumn(trainingCourse -> "₹" + trainingCourse.getTrainingCost()).setHeader("Cost").setAutoWidth(true);
        grid.addColumn(TrainingCourseDTO::getTrainerName).setHeader("Trainer Name").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(trainingCourse -> {
            Icon icon;
            if (trainingCourse.getCertificationProvided() != null && trainingCourse.getCertificationProvided()) {
                icon = VaadinIcon.CHECK_CIRCLE.create();
                icon.setColor("green");
            } else {
                icon = VaadinIcon.CLOSE_CIRCLE.create();
                icon.setColor("red");
            }
            return icon;
        })).setHeader("Certification Provided?");

        grid.addItemDoubleClickListener(event -> openCourseDetails(event.getItem()));
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void openCourseDetails(TrainingCourseDTO course) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Course Details");
        dialog.setWidth("450px");

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        NumberField requestedCostField = new NumberField("Requested Cost");
        requestedCostField.setValue(course.getTrainingCost().doubleValue());
        requestedCostField.setPrefixComponent(new Span("₹"));
        requestedCostField.setWidthFull();

        detailsLayout.add(requestedCostField);

        detailsLayout.getChildren().forEach(component -> {
            if (component instanceof Span) {
                component.getElement().getStyle().set("margin-bottom", "8px");
                component.getElement().getStyle().set("font-weight", "500");
            }
        });

        Button enrollBtn = new Button("Enroll Now", VaadinIcon.PAPERPLANE.create(), e -> {
            User user = currentUserProvider.getCurrentUser();

            Employee employee = user.getEmployee();

            boolean alreadyEnrolled = trainingEnrollmentService.isAlreadyEnrolled(employee.getEmployeeId(), course.getCourseId());
            if (alreadyEnrolled) {
                Notification notification = Notification.show("You have already enrolled in or requested this course!");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            trainingEnrollmentService.enrollEmployee(employee.getEmployeeId(), course.getCourseId(), requestedCostField.getValue());

            Notification.show("Enrollment request sent successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            dialog.close();
        });

        enrollBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(enrollBtn, closeBtn);

        dialog.add(detailsLayout);
        dialog.open();
    }

    private void updateGrid() {
        this.allCourses = trainingCourseService.getAllCourseDTOs();
        grid.setItems(allCourses);
    }

    private void filterGrid() {
        if (allCourses == null) return;

        String courseQuery = courseSearchField.getValue() != null ? courseSearchField.getValue().trim().toLowerCase() : "";
        TrainingCategoryDTO categoryQuery = categoryFilterField.getValue();

        if (courseQuery.isEmpty() && categoryQuery == null) {
            grid.setItems(allCourses);
            return;
        }

        List<TrainingCourseDTO> filteredList = allCourses.stream().filter(dto -> {
            boolean matchesName = courseQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));
            boolean matchesCategory = categoryQuery == null || (dto.getCategory() != null && categoryQuery.getCategoryId() != null && categoryQuery.getCategoryId().equals(dto.getCategory().getCategoryId()));
            return matchesName && matchesCategory;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }
}