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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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

import static java.rmi.Naming.bind;

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

        HorizontalLayout spacer = new HorizontalLayout();
        HorizontalLayout toolbar = new HorizontalLayout(courseSearchField, typeSelectField, certSelectField, spacer, addBtn, editBtn, deleteBtn);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
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
        grid.addItemDoubleClickListener(e -> openCourseForm(e.getItem()));

        grid.addColumn(TrainingCourseDTO::getCourseName).setHeader("Course Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(dto -> dto.getCategory() != null ? dto.getCategory().getCategoryName() : "").setHeader("Category").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getDurationDays).setHeader("Days").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getTrainingCost).setHeader("Cost").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getTrainingTypeName).setHeader("Type").setAutoWidth(true).setSortable(true);
        grid.addColumn(TrainingCourseDTO::getTrainerName).setHeader("Trainer").setAutoWidth(true).setSortable(true);
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
        NumberField durationDays = new NumberField("Duration (Days)");
        NumberField trainingCost = new NumberField("Training Cost");
        ComboBox<EmployeeDTO> trainer = new ComboBox<>("Trainer");
        ComboBox<TrainingTypeDTO> trainingType = new ComboBox<>("Training Type");
        Checkbox certificationProvided = new Checkbox("Certification Provided");
        IntegerField certValidity = new IntegerField("Validity (Months)");

        courseName.setWidthFull();
        description.setWidthFull();
        category.setWidthFull();
        durationDays.setWidthFull();
        trainingCost.setWidthFull();
        trainer.setWidthFull();
        trainingType.setWidthFull();
        certValidity.setWidthFull();

        category.setItems(trainingCategoryService.getAllCategoryDTOs());
        category.setItemLabelGenerator(item -> item != null ? item.getCategoryName() : "");

        trainer.setItems(employeeService.getAllTrainerDTOs());
        trainer.setItemLabelGenerator(emp -> emp != null ? emp.getFirstName() + " " + emp.getLastName() : "");

        trainingType.setItems(trainingTypeService.getAllTrainingTypeDTOs());
        trainingType.setItemLabelGenerator(item -> item != null ? item.getTrainingType() : "");

        certValidity.setVisible(false);
        certificationProvided.addValueChangeListener(e -> {
            certValidity.setVisible(e.getValue());
            if (!e.getValue()) {
                certValidity.clear();
            }
        });

        Binder<TrainingCourseDTO> binder = new Binder<>(TrainingCourseDTO.class);

        binder.forField(courseName)
                .asRequired("Course name is required.")
                .bind(TrainingCourseDTO::getCourseName, TrainingCourseDTO::setCourseName);

        binder.forField(description)
                .asRequired("Course description cannot be left blank.")
                .bind(TrainingCourseDTO::getDescription, TrainingCourseDTO::setDescription);

        binder.forField(category)
                .asRequired("Please choose a training category.")
                .bind(TrainingCourseDTO::getCategory, TrainingCourseDTO::setCategory);

        binder.forField(durationDays)
                .asRequired("Duration period is required.")
                .withValidator(val -> val != null && val > 0, "Duration must be greater than 0.")
                .bind(dtoInstance -> dtoInstance.getDurationDays() != null ? dtoInstance.getDurationDays().doubleValue() : null,
                (dtoInstance, val) -> dtoInstance.setDurationDays(val != null ? val.intValue() : 0));

        binder.forField(trainingCost)
                .asRequired("Training cost allocation cannot be empty.")
                .withValidator(val -> val != null && val >= 0, "Cost figure cannot execute as a negative integer.")
                .bind(dtoInstance -> dtoInstance.getTrainingCost() != null ? dtoInstance.getTrainingCost().doubleValue() : null,
                        (dtoInstance, val) -> dtoInstance.setTrainingCost(val != null ? BigDecimal.valueOf(val) : BigDecimal.ZERO));

        binder.forField(trainer)
                .asRequired("Assigning an active trainer profile is required.")
                .bind(TrainingCourseDTO::getTrainer, TrainingCourseDTO::setTrainer);

        binder.forField(trainingType)
                .asRequired("Please select a training delivery type format.")
                .bind(TrainingCourseDTO::getTrainingType, TrainingCourseDTO::setTrainingType);

        binder.forField(certificationProvided)
                .bind(TrainingCourseDTO::getCertificationProvided, TrainingCourseDTO::setCertificationProvided);

        binder.forField(certValidity)
                .withValidator(val -> !certificationProvided.getValue() || (val != null && val > 0), "Validity timeline is required when certification is enabled.")
                .bind(TrainingCourseDTO::getCertificationValidityMonths, TrainingCourseDTO::setCertificationValidityMonths);

        TrainingCourseDTO dto = (isEditMode) ? courseToEdit : new TrainingCourseDTO();
        binder.readBean(dto);

        Button saveBtn = new Button(isEditMode ? "Update Course" : "Save Course");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(dto)) {
                if (!certificationProvided.getValue()) {
                    dto.setCertificationValidityMonths(null);
                }

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

                Notification.show(isEditMode ? "Course Updated Successfully" : "Course Created Successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadInitialData();
            } else {
                Notification.show("Enter correct form inputs.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        HorizontalLayout metricsRow = new HorizontalLayout(durationDays, trainingCost);
        metricsRow.setWidthFull();
        HorizontalLayout configurationsRow = new HorizontalLayout(trainer, trainingType);
        configurationsRow.setWidthFull();

        layout.add(courseName, category, description, metricsRow, configurationsRow, certificationProvided, certValidity);
        dialog.add(layout);
        dialog.open();

        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(saveBtn, closeBtn);
    }
}