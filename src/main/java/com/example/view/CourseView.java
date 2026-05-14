package com.example.view;

import com.example.dto.TrainingCourseDTO;
import com.example.entity.Employee;
import com.example.service.TrainingCourseService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "courses", layout = MainLayout.class)
@PageTitle("ERP | Courses")
@RolesAllowed("EMPLOYEE")
public class CourseView extends VerticalLayout {

    private final TrainingCourseService trainingCourseService;
    private Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);
    private TextField filterText = new TextField();

    public CourseView(TrainingCourseService trainingCourseService) {
        this.trainingCourseService = trainingCourseService;
        addClassName("course-view");
        setSizeFull();

        configureGrid();
        configureFilter();

        add(filterText, grid);
        updateList();
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
        dialog.setWidth("400px");

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        detailsLayout.add(
                new Span("Name: " + course.getCourseName()),
                new Span("Duration: " + course.getDurationDays() + " Days"),
                new Span("Cost: ₹" + course.getTrainingCost()),
                new Span("Maximum Participants: " + course.getMaxParticipants()),
                new Span("Description: " + (course.getCourseDescription() != null ? course.getCourseDescription() : "No description available."))
        );

        detailsLayout.getChildren().forEach(component -> {
            if (component instanceof Span) {
                component.getElement().getStyle().set("margin-bottom", "8px");
                component.getElement().getStyle().set("font-weight", "500");
            }
        });

        Button enrollBtn = new Button("Enroll Now", VaadinIcon.PAPERPLANE.create(), e -> {
            //TO DO  enroll logic
            Notification.show("Enrollment request sent for " + course.getCourseName(), 3000, Notification.Position.MIDDLE);
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
        filterText.addValueChangeListener(e -> updateList());
    }

    private void updateList() {
        grid.setItems(trainingCourseService.findAllCourses(filterText.getValue()));
    }
}