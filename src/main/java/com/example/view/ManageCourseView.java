package com.example.view;

import com.example.dto.EmployeeDTO;
import com.example.dto.TrainingCategoryDTO;
import com.example.dto.TrainingCourseDTO;
import com.example.dto.TrainingTypeDTO;
import com.example.service.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Route(value = "manage-courses", layout = MainLayout.class)
@PageTitle("ERP | Course Management")
@RolesAllowed({"HR", "ADMIN", "SUPERADMIN"})
public class ManageCourseView extends VerticalLayout {

    private final TrainingCourseService trainingCourseService;
    private final TrainingCategoryService trainingCategoryService;
    private final TrainingTypeService trainingTypeService;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final AuthenticationContext authContext;

    private final Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);
    private final TextField filterField = new TextField();

    public ManageCourseView(TrainingCourseService trainingCourseService, TrainingCategoryService trainingCategoryService, TrainingTypeService trainingTypeService, EmployeeService employeeService, UserService userService, AuthenticationContext authContext) {
        this.trainingCourseService = trainingCourseService;
        this.trainingCategoryService = trainingCategoryService;
        this.trainingTypeService = trainingTypeService;
        this.employeeService = employeeService;
        this.userService = userService;
        this.authContext = authContext;

        setSizeFull();

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        H2 title = new H2("Course Management");

        filterField.setPlaceholder("Search Course...");
        filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(e -> updateGrid());

        Button addBtn = new Button(VaadinIcon.PLUS.create(), e -> openCourseForm(null));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> {
            TrainingCourseDTO selected = checkSingleSelection();
            if (selected != null) {
                openCourseForm(selected);
            }
        });

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            if (grid.getSelectedItems().isEmpty()) {
                Notification.show("Please select items to delete");
            } else {
                confirmBulkDelete(grid.getSelectedItems());
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterField, addBtn, editBtn, deleteBtn);
        toolbar.setWidthFull();
        toolbar.expand(filterField);

        configureGrid();
        updateGrid();

        add(title, toolbar, grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.addColumn(TrainingCourseDTO::getCourseName)
                .setHeader("Course Name")
                .setAutoWidth(true);

        grid.addColumn(trainingCourseDTO -> trainingCourseDTO.getCategory().getCategoryName())
                .setHeader("Category")
                .setWidth("250px")
                .setFlexGrow(0);

        grid.addColumn(TrainingCourseDTO::getDurationDays)
                .setHeader("Days")
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(TrainingCourseDTO::getTrainingCost)
                .setHeader("Cost")
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(trainingCourse -> trainingCourse.getTrainingType().getTrainingType())
                .setHeader("Type")
                .setWidth("200px")
                .setFlexGrow(0);

        grid.addColumn(TrainingCourseDTO::getMaxParticipants)
                .setHeader("Max Participants")
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(trainingCourseDTO -> trainingCourseDTO.getTrainer().getFirstName())
                .setHeader("Trainer")
                .setWidth("200px")
                .setFlexGrow(0);

        grid.addColumn(trainingCourseDTO -> trainingCourseDTO.getCertificationProvided() ? "YES" : "NO")
                .setHeader("Certificate Provided?")
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(TrainingCourseDTO::getCertificationValidityMonths)
                .setHeader("Certificate Expire in (Months)")
                .setWidth("250px")
                .setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("100%");
    }

    private void updateGrid() {
        String value = filterField.getValue();
        if (value == null || value.isEmpty()) {
            grid.setItems(trainingCourseService.getAllCourseDTOs());
        } else {
            grid.setItems(trainingCourseService.searchCourseDTOs(value));
        }
    }

    private TrainingCourseDTO checkSingleSelection() {
        Set<TrainingCourseDTO> selected = grid.getSelectedItems();
        if (selected.size() != 1) {
            Notification.show("Please select exactly one item");
            return null;
        }
        return selected.iterator().next();
    }

    private void confirmBulkDelete(Set<TrainingCourseDTO> courses) {
        Dialog dialog = new Dialog();
        dialog.add(new Text("Are you sure you want to delete " + courses.size() + " items?"));

        Button confirm = new Button("Delete", e -> {
            courses.forEach(c -> trainingCourseService.deleteCourse(c.getCourseId()));
            updateGrid();
            dialog.close();
            Notification.show("Deleted successfully");
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(confirm, new Button("Cancel", e -> dialog.close()));
        dialog.open();
    }

    private void openCourseForm(TrainingCourseDTO courseToEdit) {
        boolean isEditMode = (courseToEdit != null);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEditMode ? "Edit Course" : "Create New Course");
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();

        TextField courseName = new TextField("Course Name");
        TextField description = new TextField("Description");

        ComboBox<TrainingCategoryDTO> category = new ComboBox<>("Category");
        category.setItems(trainingCategoryService.getAllCategoryDTOs());
        category.setItemLabelGenerator(TrainingCategoryDTO::getCategoryName);

        NumberField durationDays = new NumberField("Duration (Days)");
        NumberField trainingCost = new NumberField("Training Cost");

        ComboBox<EmployeeDTO> trainer = new ComboBox<>("Trainer");
        trainer.setItems(employeeService.getAllTrainerDTOs());
        trainer.setItemLabelGenerator(emp -> emp.getFirstName() + " " + emp.getLastName());

        ComboBox<TrainingTypeDTO> trainingType = new ComboBox<>("Training Type");
        trainingType.setItems(trainingTypeService.getAllTrainingTypeDTOs());
        trainingType.setItemLabelGenerator(TrainingTypeDTO::getTrainingType);

        IntegerField maxParticipants = new IntegerField("Max Participants");

        Checkbox certificationProvided = new Checkbox("Certification Provided");
        IntegerField certValidity = new IntegerField("Validity (Months)");
        certValidity.setVisible(false);

        certificationProvided.addValueChangeListener(e -> certValidity.setVisible(e.getValue()));

        TrainingCourseDTO dto;

        if (isEditMode) {
            dto = courseToEdit;
            courseName.setValue(dto.getCourseName());
            description.setValue(dto.getDescription() != null ? dto.getDescription() : "");
            category.setValue(dto.getCategory());
            durationDays.setValue(dto.getDurationDays() != null ? dto.getDurationDays().doubleValue() : 0.0);
            trainingCost.setValue(dto.getTrainingCost() != null ? dto.getTrainingCost().doubleValue() : 0.0);
            trainer.setValue(dto.getTrainer());
            trainingType.setValue(dto.getTrainingType());
            maxParticipants.setValue(dto.getMaxParticipants());
            certificationProvided.setValue(dto.getCertificationProvided());

            if (dto.getCertificationProvided()) {
                certValidity.setVisible(true);
                certValidity.setValue(dto.getCertificationValidityMonths());
            }
        } else {
            dto = new TrainingCourseDTO();
        }

        Button saveBtn = new Button(isEditMode ? "Update Course" : "Save Course");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveBtn.addClickListener(e -> {
            dto.setCourseName(courseName.getValue());
            dto.setCategory(category.getValue());
            dto.setDescription(description.getValue());
            dto.setDurationDays(durationDays.getValue().intValue());
            dto.setTrainingCost(BigDecimal.valueOf(trainingCost.getValue()));
            dto.setTrainer(trainer.getValue());
            dto.setTrainingType(trainingType.getValue());
            dto.setCertificationProvided(certificationProvided.getValue());

            if (certificationProvided.getValue()) {
                dto.setCertificationValidityMonths(certValidity.getValue());
            } else {
                dto.setCertificationValidityMonths(null);
            }

            dto.setMaxParticipants(maxParticipants.getValue());

            if (!isEditMode) {
                dto.setActive(true);
                dto.setCreatedAt(LocalDateTime.now());
                authContext.getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class)
                        .ifPresent(userDetails -> {
                            userService.findByUsername(userDetails.getUsername())
                                    .ifPresent(user -> dto.setCreatedBy(userService.convertToDTO(user)));
                        });
                trainingCourseService.saveCourse(dto);
            } else trainingCourseService.updateCourse(dto);

            Notification.show(isEditMode ? "Course Updated Successfully" : "Course Created Successfully");
            dialog.close();
            updateGrid();
        });

        layout.add(courseName, category, description,
                new HorizontalLayout(durationDays, trainingCost),
                new HorizontalLayout(trainer, trainingType),
                maxParticipants, certificationProvided, certValidity, saveBtn);

        dialog.add(layout);
        dialog.open();
    }
}