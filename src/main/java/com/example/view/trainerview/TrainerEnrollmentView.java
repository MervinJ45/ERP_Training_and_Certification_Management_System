package com.example.view.trainerview;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.User;
import com.example.service.TrainingEnrollmentService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "trainer-enrollments", layout = MainLayout.class)
@PageTitle("ERP | My Class Enrollments")
@RolesAllowed({"TRAINER", "ADMIN"})
public class TrainerEnrollmentView extends VerticalLayout {

    private final TrainingEnrollmentService trainingEnrollmentService;
    private final CurrentUserProvider currentUserProvider;

    private final Grid<TrainingEnrollmentDTO> grid = new Grid<>(TrainingEnrollmentDTO.class, false);

    public TrainerEnrollmentView(TrainingEnrollmentService trainingEnrollmentService, CurrentUserProvider currentUserProvider) {
        this.trainingEnrollmentService = trainingEnrollmentService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("My Training Courseu");
        Span subTitle = new Span("Manage active students and sign-off completed courses.");
        subTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        configureGrid();
        loadData();

        add(title, subTitle, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TrainingEnrollmentDTO::getCourseName).setHeader("Course Title").setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getEmployeeFullName).setHeader("Student Name").setSortable(true);
        grid.addColumn(TrainingEnrollmentDTO::getEnrollmentStatusName).setHeader("Current Status");

        grid.addColumn(new ComponentRenderer<>(dto -> {
            Button completeBtn = new Button("Mark As Complete");
            completeBtn.setIcon(VaadinIcon.CHECK.create());
            completeBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

            completeBtn.addClickListener(e -> openCompletionDialog(dto));

            return completeBtn;
        })).setHeader("Actions").setAutoWidth(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void openCompletionDialog(TrainingEnrollmentDTO dto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Finalize Course Completion");
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Span confirmationText = new Span("Confirm completion status for " + dto.getEmployeeFullName() + " in " + dto.getCourseName() + "?");
        confirmationText.getStyle().set("font-weight", "500");

        // NEW FIELD: Proficiency Rating Selector (1 to 5 scale)
        Select<Integer> proficiencyRating = new Select<>();
        proficiencyRating.setLabel("Proficiency Rating (1-5)");
        proficiencyRating.setItems(1, 2, 3, 4, 5);
        proficiencyRating.setValue(3); // Default placeholder mid-scale value
        proficiencyRating.setWidthFull();

        TextArea remarks = new TextArea("Trainer Evaluation Remarks");
        remarks.setPlaceholder("Enter final performance feedback or grades here...");
        remarks.setWidthFull();
        remarks.setHeight("6em");

        layout.add(confirmationText, proficiencyRating, remarks);

        Button confirmBtn = new Button("Submit Completion", e -> {
            if (proficiencyRating.getValue() == null) {
                Notification.show("Please assign a Proficiency Rating before completing.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            try {
                // CHANGED: Passing both remarks AND the proficiency rating integer down to the backend engine
                trainingEnrollmentService.completeEnrollment(dto.getEnrollmentId(), remarks.getValue(), proficiencyRating.getValue());

                Notification n = Notification.show("Course execution successfully finalized!");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Error saving progression: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(confirmBtn, cancelBtn);
        dialog.open();
    }

    private void loadData() {
        User user = currentUserProvider.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            Notification.show("Session Timeout: Profile reference missing.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Long trainerId = user.getEmployee().getEmployeeId();

        List<TrainingEnrollmentDTO> activeRoster = trainingEnrollmentService.getTrainerSpecificEnrollments(trainerId);
        grid.setItems(activeRoster);
    }
}