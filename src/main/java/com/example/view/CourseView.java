package com.example.view;

import com.example.dto.TrainingCourseDTO;
import com.example.entity.Employee;
import com.example.entity.User;
import com.example.service.EmployeeService;
import com.example.service.TrainingCourseService;
import com.example.service.TrainingEnrollmentService;
import com.example.service.UserService;
import com.example.utils.CurrentUserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "courses", layout = MainLayout.class)
@PageTitle("ERP | Courses")
@RolesAllowed("EMPLOYEE")
public class CourseView extends VerticalLayout {

    private final AuthenticationContext authContext;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final TrainingEnrollmentService trainingEnrollmentService;
    private final TrainingCourseService trainingCourseService;
    private final CurrentUserProvider currentUserProvider;
    private Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);
    private TextField filterText = new TextField();

    public CourseView(TrainingCourseService trainingCourseService, AuthenticationContext authContext, UserService userService, EmployeeService employeeService, TrainingEnrollmentService trainingEnrollmentService, CurrentUserProvider currentUserProvider) {

        this.trainingCourseService = trainingCourseService;
        this.authContext = authContext;
        this.userService = userService;
        this.employeeService = employeeService;
        this.trainingEnrollmentService = trainingEnrollmentService;
        this.currentUserProvider = currentUserProvider;

        addClassName("course-view");
        setSizeFull();

        configureGrid();
        configureFilter();

        add(filterText, grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.addClassNames("course-grid");
        grid.setSizeFull();

        grid.addColumn(TrainingCourseDTO::getCourseName).setHeader("Course Name").setSortable(true).setResizable(true);
        grid.addColumn(TrainingCourseDTO::getDurationDays).setHeader("Days").setAutoWidth(true);
        grid.addColumn(trainingCourse -> "₹" + trainingCourse.getTrainingCost()).setHeader("Cost").setAutoWidth(true);
        grid.addColumn(TrainingCourseDTO::getMaxParticipants).setHeader("Max Capacity").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(trainingCourse -> {
            Icon icon;
            if (trainingCourse.getCertificationProvided()) {
                icon = VaadinIcon.CHECK_CIRCLE.create();
                icon.setColor("green");
            } else {
                icon = VaadinIcon.CLOSE_CIRCLE.create();
                icon.setColor("red");
            }
            return icon;
        })).setHeader("Certification Provided?");

        grid.addItemClickListener(event -> openCourseDetails(event.getItem()));

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

        detailsLayout.add(new Span("Name: " + course.getCourseName()), new Span("Duration: " + course.getDurationDays() + " Days"), new Span("Actual Cost: ₹" + course.getTrainingCost()), new Span("Maximum Participants: " + course.getMaxParticipants()), new Span("Description: " + (course.getCourseDescription() != null ? course.getCourseDescription() : "No description available.")), requestedCostField);

        detailsLayout.getChildren().forEach(component -> {
            if (component instanceof Span) {
                component.getElement().getStyle().set("margin-bottom", "8px");
                component.getElement().getStyle().set("font-weight", "500");
            }
        });

        Button enrollBtn = new Button("Enroll Now", VaadinIcon.PAPERPLANE.create(), e -> {
            User user = currentUserProvider.getCurrentUser();

            Employee employee = user.getEmployee();

            trainingEnrollmentService.enrollEmployee(employee.getEmployeeId(), course.getCourseId(), requestedCostField.getValue());

            Notification.show("Enrollment request sent successfully");
            dialog.close();
        });

        enrollBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(enrollBtn, closeBtn);

        dialog.add(detailsLayout);
        dialog.open();
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateGrid());
    }

    private void updateGrid() {
        String value = filterText.getValue();
        if (value == null || value.isEmpty()) {
            grid.setItems(trainingCourseService.getAllCourseDTOs());
        } else {
            grid.setItems(trainingCourseService.searchCourseDTOs(value));
        }
    }
}