package com.example.view.auditorview;

import com.example.dto.EmployeeDTO;
import com.example.entity.SkillMatrix;
import com.example.service.EmployeeService;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "reports/employee-skills-matrix", layout = MainLayout.class)
@PageTitle("ERP | Employee Skills Matrix")
@RolesAllowed("AUDITOR")
public class EmployeeSkillMatrixView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final SkillMatrixService skillMatrixService;

    private final Grid<EmployeeDTO> employeeGrid = new Grid<>();

    public EmployeeSkillMatrixView(EmployeeService employeeService, SkillMatrixService skillMatrixService) {
        this.employeeService = employeeService;
        this.skillMatrixService = skillMatrixService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 heading = new H2("Employee Skills & Matrix Overview");
        heading.getStyle().set("margin-top", "0");

        configureEmployeeGrid();
        loadEmployees();

        add(heading, new Hr(), employeeGrid);
    }

    private void configureEmployeeGrid() {
        employeeGrid.setSizeFull();

        employeeGrid.addColumn(dto -> dto.getFirstName() + " " + dto.getLastName())
                .setHeader("Full Name")
                .setSortable(true)
                .setAutoWidth(true);

        employeeGrid.addColumn(EmployeeDTO::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        employeeGrid.addColumn(dto -> dto.getDepartment() != null ? dto.getDepartment().getDepartmentName() : "N/A")
                .setHeader("Department")
                .setSortable(true)
                .setAutoWidth(true);

        employeeGrid.addColumn(EmployeeDTO::getDesignation)
                .setHeader("Designation")
                .setSortable(true)
                .setAutoWidth(true);

        employeeGrid.setDetailsVisibleOnClick(true);
        employeeGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createSkillMatrixDropdownContent));
    }

    private VerticalLayout createSkillMatrixDropdownContent(EmployeeDTO employee) {
        VerticalLayout dropdownContainer = new VerticalLayout();
        dropdownContainer.setPadding(true);
        dropdownContainer.setSpacing(true);
        dropdownContainer.getStyle()
                .set("background-color", "#F8FAFC")
                .set("border-left", "4px solid #0EA5E9")
                .set("margin", "5px 0px");

        HorizontalLayout dropdownHeader = new HorizontalLayout(
                VaadinIcon.SITEMAP.create(),
                new Span("Skill Matrix for " + employee.getFirstName() + " " + employee.getLastName())
        );
        dropdownHeader.getStyle().set("font-weight", "bold").set("color", "#0F172A");
        dropdownContainer.add(dropdownHeader);

        List<SkillMatrix> employeeSkills = skillMatrixService.getSkillsByEmployeeId(employee.getEmployeeId());

        if (employeeSkills == null || employeeSkills.isEmpty()) {
            Span noSkillsMessage = new Span("No mapped skills found for this employee.");
            noSkillsMessage.getStyle().set("font-style", "italic").set("color", "#94A3B8").set("padding-left", "5px");
            dropdownContainer.add(noSkillsMessage);
        } else {
            Grid<SkillMatrix> grid = new Grid<>();
            grid.setAllRowsVisible(true);
            grid.setItems(employeeSkills);

            grid.addColumn(SkillMatrix::getSkillName)
                    .setHeader("Skill Name")
                    .setAutoWidth(true);

            grid.addColumn(skill -> skill.getCourse() != null ? skill.getCourse().getCourseName() : "Direct Assessment")
                    .setHeader("Source Course")
                    .setAutoWidth(true);

            grid.addColumn(skill -> "★ ".repeat(Math.max(0, skill.getProficiencyRating())) + "☆ ".repeat(Math.max(0, 5 - skill.getProficiencyRating()))).setHeader("Proficiency Rating").setAutoWidth(true);

            grid.addColumn(skill -> skill.getUpdatedAt() != null ?
                            skill.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A")
                    .setHeader("Last Updated")
                    .setAutoWidth(true);

            dropdownContainer.add(grid);
        }

        return dropdownContainer;
    }


    private void loadEmployees() {
        List<EmployeeDTO> allEmployees = employeeService.getEmployeesWithSkills();
        employeeGrid.setItems(allEmployees);
    }
}