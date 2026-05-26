package com.example.view.superadminview;

import com.example.dto.TrainingCategoryDTO;
import com.example.dto.TrainingCourseDTO;
import com.example.service.TrainingCategoryService;
import com.example.service.TrainingCourseService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "training-courses", layout = MainLayout.class)
@PageTitle("Training Courses")
@RolesAllowed("SUPER_ADMIN")
public class TrainingCourseListView extends VerticalLayout {

    private final Grid<TrainingCourseDTO> grid = new Grid<>(TrainingCourseDTO.class, false);
    private final TrainingCourseService trainingCourseService;
    private final TrainingCategoryService trainingCategoryService;

    private final TextField courseSearchField = new TextField();
    private final ComboBox<TrainingCategoryDTO> categoryFilterField = new ComboBox<>();

    private List<TrainingCourseDTO> allCourses;

    public TrainingCourseListView(TrainingCourseService trainingCourseService, TrainingCategoryService trainingCategoryService) {
        this.trainingCourseService = trainingCourseService;
        this.trainingCategoryService = trainingCategoryService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title  = new H2("Course List");

        configureFilters();
        configureGrid();

        HorizontalLayout spacer = new HorizontalLayout();

        HorizontalLayout toolbar = new HorizontalLayout(courseSearchField, categoryFilterField, spacer);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.expand(spacer);

        add(title, toolbar, grid);
        loadInitialData();
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
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TrainingCourseDTO::getCourseName).setHeader("Course Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(this::getCategoryNameSafe).setHeader("Category").setSortable(true).setAutoWidth(true);
        grid.addColumn(dto -> dto.getTrainerName() != null ? dto.getTrainerName() : (dto.getTrainer() != null ? dto.getTrainer().getFirstName() : "Unassigned")).setHeader("Trainer").setAutoWidth(true);
        grid.addColumn(TrainingCourseDTO::getDurationDays).setHeader("Duration (Days)").setSortable(true).setAutoWidth(true);
        grid.addColumn(new NumberRenderer<>(TrainingCourseDTO::getTrainingCost, NumberFormat.getCurrencyInstance(new Locale("en","IN")))).setHeader("Cost").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createCertificationBadge)).setHeader("Certification").setAutoWidth(true);
    }

    private String getCategoryNameSafe(TrainingCourseDTO dto) {
        if (dto.getCategoryName() != null) {
            return dto.getCategoryName();
        } else if (dto.getCategory() != null) {
            return dto.getCategory().getCategoryName();
        }
        return "N/A";
    }

    private Span createCertificationBadge(TrainingCourseDTO dto) {
        Span badge = new Span();
        if (Boolean.TRUE.equals(dto.getCertificationProvided())) {
            String text = "Yes";
            if (dto.getCertificationValidityMonths() != null) {
                text += String.format(" (%d mo)", dto.getCertificationValidityMonths());
            }
            badge.setText(text);
            badge.getElement().getThemeList().add("badge success primary");
        } else {
            badge.setText("No");
            badge.getElement().getThemeList().add("badge contrast");
        }
        return badge;
    }

    private void loadInitialData() {
        allCourses = trainingCourseService.getAllCourseDTOs();
        filterGrid();
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

    public void loadGrid() {
        loadInitialData();
    }
}