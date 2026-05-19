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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "skill-matrix-summary", layout = MainLayout.class)
@PageTitle("ERP | Skill Matrix Summary")
@RolesAllowed({"ADMIN", "HR", "MANAGER"})
public class SkillMatrixSummaryView extends VerticalLayout {

    private final SkillMatrixService skillMatrixService;
    private final Grid<SkillSummaryDTO> grid = new Grid<>();
    private final TextField searchField = new TextField();
    private List<SkillSummaryDTO> allData;

    public SkillMatrixSummaryView(SkillMatrixService skillMatrixService) {
        this.skillMatrixService = skillMatrixService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Corporate Skill Inventory Matrix");
        Span description = new Span("Real-time summary breakdown of employee proficiencies derived from completed course executions.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        configureSearch();
        configureGrid();

        add(title, description, searchField, grid);
        refreshGrid();
    }

    private void configureSearch() {
        searchField.setPlaceholder("Search by Employee, Course, or Skill...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.setSizeFull();
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
                    star.getColor();
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
        })).setHeader("Proficiency Rating").setSortable(true).setComparator((a, b) -> Integer.compare(a.getProficiencyRating(), b.getProficiencyRating()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        grid.addColumn(dto -> dto.getUpdatedAt() != null ? dto.getUpdatedAt().format(formatter) : "N/A").setHeader("Last Verified").setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void refreshGrid() {
        allData = skillMatrixService.getSkillMatrixSummary();
        grid.setItems(allData);
    }

    private void filterGrid() {
        String query = searchField.getValue();
        if (query == null || query.trim().isEmpty()) {
            grid.setItems(allData);
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        List<SkillSummaryDTO> filteredList = allData.stream().filter(dto -> (dto.getEmployeeFullName() != null && dto.getEmployeeFullName().toLowerCase().contains(lowerCaseQuery)) || (dto.getCourseName() != null && dto.getCourseName().toLowerCase().contains(lowerCaseQuery)) || (dto.getSkillName() != null && dto.getSkillName().toLowerCase().contains(lowerCaseQuery))).collect(Collectors.toList());
        grid.setItems(filteredList);
    }
}