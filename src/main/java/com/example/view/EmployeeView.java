package com.example.view;

import com.example.dto.EmployeeDTO;
import com.example.dto.DepartmentDTO;
import com.example.dto.RoleDTO;
import com.example.entity.TrainingCourse;
import com.example.service.DepartmentService;
import com.example.service.EmployeeService;
import com.example.service.RoleService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Set;
import java.util.UUID;

@Route(value = "employee", layout = MainLayout.class)
@PageTitle("Employees")
@RolesAllowed("SUPERADMIN")
public class EmployeeView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final RoleService roleService;

    private final Grid<EmployeeDTO> grid = new Grid<>(EmployeeDTO.class, false);

    public EmployeeView(EmployeeService employeeService, DepartmentService departmentService, RoleService roleService) {

        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.roleService = roleService;

        setSizeFull();

        H2 title = new H2("Employee Management");

        TextField filterField = new TextField();
        filterField.setPlaceholder("Search Employee");
        filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterField.setValueChangeMode(ValueChangeMode.EAGER);

        Button addEmployeeBtn = new Button("Create Employee");

        HorizontalLayout toolbar = new HorizontalLayout(filterField, addEmployeeBtn);

        toolbar.setWidthFull();
        toolbar.expand(filterField);


        configureGrid();

        updateGrid();

        filterField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value == null || value.isEmpty()) {
                grid.setItems(employeeService.getAllEmployeeDTOs());
            } else {
                grid.setItems(employeeService.searchEmployeeDTOs(value));
            }
        });

        addEmployeeBtn.addClickListener(e -> openEmployeeForm(null));
        Scroller gridScroller = new Scroller(grid);
        gridScroller.setSizeFull();
        gridScroller.setScrollDirection(Scroller.ScrollDirection.BOTH);
        add(title, toolbar, gridScroller);
        expand(gridScroller);
    }

    private void configureGrid() {
        grid.addColumn(EmployeeDTO::getFirstName).setHeader("First Name").setWidth("180px").setFrozen(true);
        grid.addColumn(EmployeeDTO::getLastName).setHeader("Last Name").setWidth("180px").setFlexGrow(0);
        grid.addColumn(EmployeeDTO::getEmail).setHeader("Email").setWidth("250px").setFlexGrow(0);
        grid.addColumn(EmployeeDTO::getPhone).setHeader("Phone").setWidth("180px").setFlexGrow(0);

        grid.addColumn(dto -> dto.getDepartment() != null ?
                        dto.getDepartment().getDepartmentName() : "No Dept")
                .setHeader("Department").setWidth("200px").setFlexGrow(0);

        grid.addColumn(dto -> {
            if (dto.getManager() != null) {
                return dto.getManager().getFirstName() + " " + dto.getManager().getLastName();
            }
            return "No Manager";
        }).setHeader("Manager").setWidth("220px").setFlexGrow(0);

        grid.addColumn(EmployeeDTO::getDesignation).setHeader("Designation").setWidth("200px").setFlexGrow(0);
        grid.addColumn(EmployeeDTO::getDateOfJoining).setHeader("Date Of Joining").setWidth("180px").setFlexGrow(0);

        grid.addColumn(dto -> dto.getRole() != null ?
                        dto.getRole().getRoleName() : "No Role")
                .setHeader("Role").setWidth("180px").setFlexGrow(0);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("100%");

        grid.addItemClickListener(employeeDTO -> {
            openEmployeeForm(employeeDTO.getItem());
        });

    }

    private void updateGrid() {

        grid.setItems(employeeService.getAllEmployeeDTOs());
    }

    private void openEmployeeForm(EmployeeDTO employeeDTO) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle(employeeDTO == null ? "Add New Employee" : "Update Employee");

        VerticalLayout layout = new VerticalLayout();

        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");
        EmailField email = new EmailField("Email");
        TextField phone = new TextField("Phone");
        TextField designation = new TextField("Designation");
        DatePicker joiningDate = new DatePicker("Date Of Joining");

        ComboBox<DepartmentDTO> department = new ComboBox<>("Department");
        department.setItems(departmentService.getAllDepartmentDTOs());
        department.setItemLabelGenerator(DepartmentDTO::getDepartmentName);

        ComboBox<RoleDTO> role = new ComboBox<>("Role");
        role.setItems(roleService.getAllRoleDTOs());
        role.setItemLabelGenerator(RoleDTO::getRoleName);

        ComboBox<EmployeeDTO> manager = new ComboBox<>("Manager");
        manager.setItems(employeeService.getAllManagerDTOs());
        manager.setItemLabelGenerator(dto -> dto.getFirstName() + " " + dto.getLastName());

        role.addValueChangeListener(e -> {
            boolean isManagerRole = e.getValue() != null && e.getValue().getRoleName().equalsIgnoreCase("MANAGER");
            manager.setVisible(!isManagerRole);
            if (isManagerRole) manager.clear();
        });

        if (employeeDTO != null) {
            firstName.setValue(employeeDTO.getFirstName() != null ? employeeDTO.getFirstName() : "");
            lastName.setValue(employeeDTO.getLastName() != null ? employeeDTO.getLastName() : "");
            email.setValue(employeeDTO.getEmail() != null ? employeeDTO.getEmail() : "");
            phone.setValue(employeeDTO.getPhone() != null ? employeeDTO.getPhone() : "");
            designation.setValue(employeeDTO.getDesignation() != null ? employeeDTO.getDesignation() : "");
            joiningDate.setValue(employeeDTO.getDateOfJoining());
            department.setValue(employeeDTO.getDepartment());
            role.setValue(employeeDTO.getRole());
            manager.setValue(employeeDTO.getManager());
        }

        Button saveBtn = new Button(employeeDTO == null ? "SAVE" : "UPDATE");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveBtn.addClickListener(e -> {
            EmployeeDTO dtoToSave = (employeeDTO == null) ? new EmployeeDTO() : employeeDTO;

            dtoToSave.setFirstName(firstName.getValue());
            dtoToSave.setLastName(lastName.getValue());
            dtoToSave.setEmail(email.getValue());
            dtoToSave.setPhone(phone.getValue());
            dtoToSave.setDesignation(designation.getValue());
            dtoToSave.setDateOfJoining(joiningDate.getValue());
            dtoToSave.setDepartment(department.getValue());
            dtoToSave.setRole(role.getValue());
            dtoToSave.setIsActive(true);

            if (manager.isVisible()) {
                dtoToSave.setManager(manager.getValue());
            }

            if (employeeDTO == null) {
                dtoToSave.setUsername(firstName.getValue().toLowerCase() + lastName.getValue().toLowerCase());
                dtoToSave.setPassword("password123");
            }

            if(employeeDTO == null){
                employeeService.registerEmployee(dtoToSave);
            }
            else employeeService.updateEmployee(dtoToSave);

            Notification.show(employeeDTO == null ? "Employee Registered!" : "Employee Updated!");
            dialog.close();
            updateGrid();
        });

        Button cancelBtn = new Button("Cancel", click -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        layout.add(firstName, lastName, email, phone, designation, joiningDate, department, role, manager, buttonLayout);

        dialog.add(layout);
        dialog.open();
    }
}

