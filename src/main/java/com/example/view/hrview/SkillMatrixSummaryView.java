package com.example.view.hrview;

import com.example.dto.SkillSummaryDTO;
import com.example.service.SkillMatrixService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "skill-matrix-summary", layout = MainLayout.class)
@PageTitle("ERP | Skill Matrix Summary")
@RolesAllowed({"ADMIN", "HR", "MANAGER"})
public class SkillMatrixSummaryView extends VerticalLayout {

    private final SkillMatrixService skillMatrixService;

    private final Grid<SkillSummaryDTO> grid = new Grid<>(SkillSummaryDTO.class, false);
    private final TextField nameSearchField = new TextField();
    private final TextField courseSearchField = new TextField();
    private final Select<Integer> proficiencySelectField = new Select<>();
    private List<SkillSummaryDTO> allData;

    public SkillMatrixSummaryView(SkillMatrixService skillMatrixService) {
        this.skillMatrixService = skillMatrixService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Skill Matrix");

        configureNameSearch();
        configureCourseSearch();
        configureProficiencyFilter();
        configureGrid();

        HorizontalLayout filterActionLayout = new HorizontalLayout(nameSearchField, courseSearchField, proficiencySelectField);
        filterActionLayout.setWidthFull();
        filterActionLayout.setSpacing(true);

        grid.setSizeFull();

        add(title, filterActionLayout, grid);
        refreshGrid();
    }

    private void configureNameSearch() {
        nameSearchField.setPlaceholder("Search by Employee");
        nameSearchField.setClearButtonVisible(true);
        nameSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        nameSearchField.setWidth("200px");
        nameSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        nameSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureCourseSearch() {
        courseSearchField.setPlaceholder("Search by Course");
        courseSearchField.setClearButtonVisible(true);
        courseSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        courseSearchField.setWidth("200px");
        courseSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        courseSearchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureProficiencyFilter() {
        proficiencySelectField.setPlaceholder("Rating (All)");
        proficiencySelectField.setEmptySelectionAllowed(true);
        proficiencySelectField.setEmptySelectionCaption("Rating (All)");
        proficiencySelectField.setItems(1, 2, 3, 4, 5);
        proficiencySelectField.setWidth("150px");

        proficiencySelectField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.addColumn(SkillSummaryDTO::getEmployeeFullName).setHeader("Employee Name").setSortable(true);
        grid.addColumn(SkillSummaryDTO::getCourseName).setHeader("Source Course").setSortable(true);
        grid.addColumn(SkillSummaryDTO::getSkillName).setHeader("Acquired Skill").setSortable(true);

        grid.addColumn(new ComponentRenderer<>(dto -> {
                    HorizontalLayout ratingLayout = new HorizontalLayout();
                    ratingLayout.setSpacing(false);

                    int rating = dto.getProficiencyRating() != null ? dto.getProficiencyRating() : 0;

                    for (int i = 1; i <= 5; i++) {
                        Icon star = VaadinIcon.STAR.create();
                        star.setSize("16px");
                        if (i <= rating) {
                            star.getStyle().set("color", "var(--lumo-error-color)");
                        } else {
                            star.getStyle().set("color", "var(--lumo-contrast-20pct)");
                        }
                        ratingLayout.add(star);
                    }

                    Span numericLabel = new Span(" (" + rating + "/5)");
                    numericLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");
                    numericLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
                    ratingLayout.add(numericLabel);

                    return ratingLayout;
                })).setHeader("Proficiency Rating")
                .setSortable(true)
                .setComparator(Comparator.comparingInt(a -> a.getProficiencyRating() != null ? a.getProficiencyRating() : 0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        grid.addColumn(dto -> dto.getUpdatedAt() != null ? dto.getUpdatedAt().format(formatter) : "N/A")
                .setHeader("Last Verified")
                .setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void refreshGrid() {
        allData = skillMatrixService.getSkillMatrixSummary();
        grid.setItems(allData);
    }

    private void filterGrid() {
        String nameQuery = nameSearchField.getValue() != null ? nameSearchField.getValue().trim().toLowerCase() : "";
        String courseQuery = courseSearchField.getValue() != null ? courseSearchField.getValue().trim().toLowerCase() : "";
        Integer ratingQuery = proficiencySelectField.getValue();

        if (nameQuery.isEmpty() && courseQuery.isEmpty() && ratingQuery == null) {
            grid.setItems(allData);
            return;
        }

        List<SkillSummaryDTO> filteredList = allData.stream().filter(dto -> {
            boolean matchesName = nameQuery.isEmpty() ||
                    (dto.getEmployeeFullName() != null && dto.getEmployeeFullName().toLowerCase().contains(nameQuery));

            boolean matchesCourse = courseQuery.isEmpty() ||
                    (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(courseQuery));

            boolean matchesRating = ratingQuery == null ||
                    (dto.getProficiencyRating() != null && dto.getProficiencyRating().equals(ratingQuery));

            return matchesName && matchesCourse && matchesRating;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }
}