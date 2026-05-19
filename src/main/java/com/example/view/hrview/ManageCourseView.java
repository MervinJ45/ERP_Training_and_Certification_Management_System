package com.example.view.hrview;

import com.example.dto.EmployeeDTO;
import com.example.dto.TrainingCategoryDTO;
import com.example.dto.TrainingCourseDTO;
import com.example.dto.TrainingTypeDTO;
import com.example.service.*;
import com.example.view.mainview.MainLayout;
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
import com.vaadin.flow.component.select.Select;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "manage-courses", layout = MainLayout.class)
@PageTitle("ERP | Course Management")
@RolesAllowed({"HR", "ADMIN", "SUPER_ADMIN"})
public class ManageCourseView extends VerticalLayout {

    private final TrainingCourseService trainingCourseService;
    private final TrainingCategoryService trainingCategoryService;
    private final TrainingTypeService trainingTypeService;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final AuthenticationContext authContext;

    private final Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);

    private final TextField courseSearchField = new TextField();
    private final Select<String> typeSelectField = new Select<>();
    private final Select<String> certSelectField = new Select<>();

    private List<TrainingCourseDTO> allData;

    public ManageCourseView(TrainingCourseService trainingCourseService, TrainingCategoryService trainingCategoryService, TrainingTypeService trainingTypeService, EmployeeService employeeService, UserService userService, AuthenticationContext authContext) {
        this.trainingCourseService = trainingCourseService;
        this.trainingCategoryService = trainingCategoryService;
        this.trainingTypeService = trainingTypeService;
        this.employeeService = employeeService;
        this.userService = userService;
        this.authContext = authContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        H2 title = new H2("Course Management");

        configureCourseSearch();
        configureTypeFilter();
        configureCertFilter();
        configureGrid();

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

        // Create an expanding spacer to push the buttons to the right edge
        HorizontalLayout spacer = new HorizontalLayout();

        // Assemble the layout with filters on the left, spacer in middle, buttons on right
        HorizontalLayout toolbar = new HorizontalLayout(courseSearchField, typeSelectField, certSelectField, spacer, addBtn, editBtn, deleteBtn);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);

        // Tell the toolbar to expand the empty space, driving the action buttons rightward
        toolbar.expand(spacer);

        grid.setSizeFull();

        add(title, toolbar, grid);
        loadInitialData();
    }

    private void configureCourseSearch() {
        courseSearchField.setPlaceholder("Search Course...");
        courseSearchField.setClearButtonVisible(true);
        courseSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        courseSearchField.setWidth("200px");
        courseSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        courseSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureTypeFilter() {
        typeSelectField.setPlaceholder("Type (All)");
        typeSelectField.setEmptySelectionAllowed(true);
        typeSelectField.setEmptySelectionCaption("Type (All)");
        typeSelectField.setItems("Online", "Classroom", "Hybrid");
        typeSelectField.setWidth("180px");
        typeSelectField.addValueChangeListener(e -> filterGrid());
    }

    private void configureCertFilter() {
        certSelectField.setPlaceholder("Certification (All)");
        certSelectField.setEmptySelectionAllowed(true);
        certSelectField.setEmptySelectionCaption("Certification (All)");
        certSelectField.setItems("Yes", "No");
        certSelectField.setWidth("180px");
        certSelectField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.addColumn(TrainingCourseDTO::getCourseName).setHeader("Course Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(dto -> dto.getCategory() != null ? dto.getCategory().getCategoryName() : "").setHeader("Category").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getDurationDays).setHeader("Days").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getTrainingCost).setHeader("Cost").setAutoWidth(true).setSortable(true);
        grid.addColumn(dto -> dto.getTrainingType() != null ? dto.getTrainingType().getTrainingType() : "").setHeader("Type").setAutoWidth(true).setSortable(true);
        grid.addColumn(dto -> dto.getTrainer() != null ? dto.getTrainer().getFirstName() : "").setHeader("Trainer").setAutoWidth(true).setSortable(true);
        grid.addColumn(dto -> dto.getCertificationProvided() != null && dto.getCertificationProvided() ? "YES" : "NO").setHeader("Certificate Provided?").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getCertificationValidityMonths).setHeader("Certificate Expire in (Months)").setAutoWidth(true).setSortable(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void loadInitialData() {
        allData = trainingCourseService.getAllCourseDTOs();
        filterGrid();
    }

    private void filterGrid() {
        if (allData == null) return;

        String nameQuery = courseSearchField.getValue() != null ? courseSearchField.getValue().trim().toLowerCase() : "";
        String typeQuery = typeSelectField.getValue();
        String certQuery = certSelectField.getValue();

        if (nameQuery.isEmpty() && typeQuery == null && certQuery == null) {
            grid.setItems(allData);
            return;
        }

        List<TrainingCourseDTO> filteredList = allData.stream().filter(dto -> {
            boolean matchesName = nameQuery.isEmpty() || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(nameQuery));

            boolean matchesType = typeQuery == null || (dto.getTrainingType() != null && typeQuery.equalsIgnoreCase(dto.getTrainingType().getTrainingType()));

            boolean matchesCert = certQuery == null || (dto.getCertificationProvided() != null && ((certQuery.equals("Yes") && dto.getCertificationProvided()) || (certQuery.equals("No") && !dto.getCertificationProvided())));

            return matchesName && matchesType && matchesCert;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
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
            courses.forEach(c -> {
                if (c != null && c.getCourseId() != null) {
                    trainingCourseService.deleteCourse(c.getCourseId());
                }
            });
            dialog.close();
            Notification.show("Deleted successfully");
            loadInitialData();
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
        category.setItemLabelGenerator(item -> item != null ? item.getCategoryName() : "");

        NumberField durationDays = new NumberField("Duration (Days)");
        NumberField trainingCost = new NumberField("Training Cost");

        ComboBox<EmployeeDTO> trainer = new ComboBox<>("Trainer");
        trainer.setItems(employeeService.getAllTrainerDTOs());
        trainer.setItemLabelGenerator(emp -> emp != null ? emp.getFirstName() + " " + emp.getLastName() : "");

        ComboBox<TrainingTypeDTO> trainingType = new ComboBox<>("Training Type");
        trainingType.setItems(trainingTypeService.getAllTrainingTypeDTOs());
        trainingType.setItemLabelGenerator(item -> item != null ? item.getTrainingType() : "");

        IntegerField maxParticipants = new IntegerField("Max Participants");

        Checkbox certificationProvided = new Checkbox("Certification Provided");
        IntegerField certValidity = new IntegerField("Validity (Months)");
        certValidity.setVisible(false);

        certificationProvided.addValueChangeListener(e -> certValidity.setVisible(e.getValue()));

        TrainingCourseDTO dto;

        if (isEditMode) {
            dto = courseToEdit;
            courseName.setValue(dto.getCourseName() != null ? dto.getCourseName() : "");
            description.setValue(dto.getDescription() != null ? dto.getDescription() : "");
            category.setValue(dto.getCategory());
            durationDays.setValue(dto.getDurationDays() != null ? dto.getDurationDays().doubleValue() : 0.0);
            trainingCost.setValue(dto.getTrainingCost() != null ? dto.getTrainingCost().doubleValue() : 0.0);
            trainer.setValue(dto.getTrainer());
            trainingType.setValue(dto.getTrainingType());
            maxParticipants.setValue(dto.getMaxParticipants() != null ? dto.getMaxParticipants() : 0);
            certificationProvided.setValue(dto.getCertificationProvided() != null && dto.getCertificationProvided());

            if (dto.getCertificationProvided() != null && dto.getCertificationProvided()) {
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

            dto.setDurationDays(durationDays.getValue() != null ? durationDays.getValue().intValue() : 0);
            dto.setTrainingCost(trainingCost.getValue() != null ? BigDecimal.valueOf(trainingCost.getValue()) : BigDecimal.ZERO);

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
                authContext.getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class).ifPresent(userDetails -> {
                    userService.findByUsername(userDetails.getUsername()).ifPresent(user -> dto.setCreatedBy(userService.convertToDTO(user)));
                });
                trainingCourseService.saveCourse(dto);
            } else {
                trainingCourseService.updateCourse(dto);
            }

            Notification.show(isEditMode ? "Course Updated Successfully" : "Course Created Successfully");
            dialog.close();
            loadInitialData();
        });

        layout.add(courseName, category, description, new HorizontalLayout(durationDays, trainingCost), new HorizontalLayout(trainer, trainingType), maxParticipants, certificationProvided, certValidity, saveBtn);

        dialog.add(layout);
        dialog.open();
    }
}