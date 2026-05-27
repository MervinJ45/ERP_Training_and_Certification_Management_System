package com.example.view.auditorview;

import com.example.dto.DepartmentDTO;
import com.example.entity.SkillMatrix;
import com.example.service.DepartmentService;
import com.example.service.SkillMatrixService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "reports/department-skills-matrix", layout = MainLayout.class)
@PageTitle("ERP | Department Skills Matrix")
@RolesAllowed("AUDITOR")
public class DepartmentSkillMatrixView extends VerticalLayout {

    private final DepartmentService departmentService;
    private final SkillMatrixService skillMatrixService;

    private final Grid<DepartmentDTO> departmentGrid = new Grid<>();

    private Map<Long, List<SkillMatrix>> departmentSkillsMap = Collections.emptyMap();

    public DepartmentSkillMatrixView(DepartmentService departmentService, SkillMatrixService skillMatrixService) {
        this.departmentService = departmentService;
        this.skillMatrixService = skillMatrixService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 heading = new H2("Department-wise Skill Matrix");
        heading.getStyle().set("margin-top", "0");

        configureDepartmentGrid();
        loadData();

        add(heading, new Hr());
    }

    private void configureDepartmentGrid() {
        departmentGrid.setSizeFull();

        departmentGrid.addColumn(DepartmentDTO::getDepartmentName).setHeader("Department Name").setSortable(true).setAutoWidth(true);

        departmentGrid.setDetailsVisibleOnClick(true);
        departmentGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDepartmentSkillContent));
    }

    private VerticalLayout createDepartmentSkillContent(DepartmentDTO department) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.getStyle().set("background-color", "#F1F5F9").set("border-radius", "8px").set("border-left", "5px solid #6366F1");

        HorizontalLayout header = new HorizontalLayout(VaadinIcon.CHART_3D.create(), new Span("Consolidated Skill Matrix: " + department.getDepartmentName()));
        header.getStyle().set("font-weight", "bold").set("color", "#4338CA");
        container.add(header);

        List<SkillMatrix> departmentSkills = departmentSkillsMap.getOrDefault(department.getDepartmentId(), Collections.emptyList());

        if (departmentSkills.isEmpty()) {
            container.add(new Span("No active employee skill records found for this department."));
        } else {
            Grid<SkillMatrix> grid = new Grid<>();
            grid.setAllRowsVisible(true);
            grid.setItems(departmentSkills);

            grid.addColumn(s -> s.getEmployee().getFirstName() + " " + s.getEmployee().getLastName()).setHeader("Employee").setSortable(true).setAutoWidth(true);
            grid.addColumn(SkillMatrix::getSkillName).setHeader("Skill / Competency").setSortable(true).setAutoWidth(true);
            grid.addColumn(skill -> "★ ".repeat(Math.max(0, skill.getProficiencyRating())) + "☆ ".repeat(Math.max(0, 5 - skill.getProficiencyRating()))).setHeader("Proficiency Rating").setAutoWidth(true);
            grid.addColumn(s -> s.getEmployee().getDesignation()).setHeader("Designation").setAutoWidth(true);

            container.add(grid);
        }

        return container;
    }

    private void loadData() {
        departmentGrid.setItems(departmentService.getAllDepartmentDTOs());

        List<SkillMatrix> allSkills = skillMatrixService.getAllSkills();

        if (allSkills != null) {
            this.departmentSkillsMap = allSkills.stream().filter(skill -> skill.getEmployee() != null && skill.getEmployee().getIsActive() && skill.getEmployee().getDepartment() != null).collect(Collectors.groupingBy(skill -> skill.getEmployee().getDepartment().getDepartmentId()));
        }
    }
}