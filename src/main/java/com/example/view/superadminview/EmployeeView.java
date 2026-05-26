package com.example.view.superadminview;

import com.example.dto.EmployeeDTO;
import com.example.dto.DepartmentDTO;
import com.example.dto.RoleDTO;
import com.example.service.DepartmentService;
import com.example.service.EmployeeService;
import com.example.service.RoleService;
import com.example.view.mainview.MainLayout;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "employee", layout = MainLayout.class)
@PageTitle("Employees")
@RolesAllowed("SUPER_ADMIN")
public class EmployeeView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final RoleService roleService;

    private final Grid<EmployeeDTO> grid = new Grid<>(EmployeeDTO.class, false);

    private final TextField firstNameFilter = new TextField();
    private final TextField emailFilter = new TextField();
    private final ComboBox<DepartmentDTO> deptFilter = new ComboBox<>();

    private List<EmployeeDTO> allEmployees;

    public EmployeeView(EmployeeService employeeService, DepartmentService departmentService, RoleService roleService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.roleService = roleService;

        setSizeFull();

        H2 title = new H2("Employee Management");

        configureFilters();
        configureGrid();

        Button addEmployeeBtn = new Button("Create Employee", VaadinIcon.PLUS.create());
        addEmployeeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addEmployeeBtn.addClickListener(e -> openEmployeeForm(null));

        HorizontalLayout spacer = new HorizontalLayout();

        HorizontalLayout toolbar = new HorizontalLayout(firstNameFilter, emailFilter, deptFilter, spacer, addEmployeeBtn);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.expand(spacer);

        Scroller gridScroller = new Scroller(grid);
        gridScroller.setSizeFull();
        gridScroller.setScrollDirection(Scroller.ScrollDirection.BOTH);

        add(title, toolbar, gridScroller);
        expand(gridScroller);

        loadInitialData();
    }

    private void configureFilters() {
        firstNameFilter.setPlaceholder("Filter by First Name");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        firstNameFilter.setWidth("200px");
        firstNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        firstNameFilter.addValueChangeListener(e -> filterGrid());

        emailFilter.setPlaceholder("Filter by Email");
        emailFilter.setClearButtonVisible(true);
        emailFilter.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailFilter.setWidth("200px");
        emailFilter.setValueChangeMode(ValueChangeMode.LAZY);
        emailFilter.addValueChangeListener(e -> filterGrid());

        deptFilter.setPlaceholder("Department (All)");
        deptFilter.setClearButtonVisible(true);
        deptFilter.setWidth("220px");
        deptFilter.setItems(departmentService.getAllDepartmentDTOs());
        deptFilter.setItemLabelGenerator(item -> item != null ? item.getDepartmentName() : "");
        deptFilter.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.addColumn(EmployeeDTO::getFirstName).setHeader("First Name").setFrozen(true).setSortable(true).setAutoWidth(true);
        grid.addColumn(EmployeeDTO::getLastName).setHeader("Last Name").setFlexGrow(0).setSortable(true).setAutoWidth(true);
        grid.addColumn(EmployeeDTO::getEmail).setHeader("Email").setFlexGrow(0).setSortable(true).setAutoWidth(true);
        grid.addColumn(EmployeeDTO::getPhone).setHeader("Phone").setAutoWidth(true);

        grid.addColumn(dto -> dto.getDepartment() != null ? dto.getDepartment().getDepartmentName() : "No Dept").setHeader("Department").setWidth("200px").setFlexGrow(0).setSortable(true);

        grid.addColumn(dto -> {
            if (dto.getManager() != null) {
                return dto.getManager().getFirstName() + " " + dto.getManager().getLastName();
            }
            return "No Manager";
        }).setHeader("Manager").setAutoWidth(true);

        grid.addColumn(EmployeeDTO::getDesignation).setHeader("Designation").setAutoWidth(true);
        grid.addColumn(EmployeeDTO::getDateOfJoining).setHeader("Date Of Joining").setAutoWidth(true).setSortable(true);

        grid.addColumn(dto -> dto.getRole() != null ? dto.getRole().getRoleName() : "No Role").setHeader("Role").setAutoWidth(true).setFlexGrow(0).setSortable(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("100%");

        grid.addItemClickListener(employeeDTO -> {
            openEmployeeForm(employeeDTO.getItem());
        });
    }

    private void loadInitialData() {
        allEmployees = employeeService.getAllEmployeeDTOs();
        filterGrid();
    }

    private void filterGrid() {
        if (allEmployees == null) return;

        String nameQuery = firstNameFilter.getValue() != null ? firstNameFilter.getValue().trim().toLowerCase() : "";
        String emailQuery = emailFilter.getValue() != null ? emailFilter.getValue().trim().toLowerCase() : "";
        DepartmentDTO deptQuery = deptFilter.getValue();

        if (nameQuery.isEmpty() && emailQuery.isEmpty() && deptQuery == null) {
            grid.setItems(allEmployees);
            return;
        }

        List<EmployeeDTO> filteredList = allEmployees.stream().filter(dto -> {
            boolean matchesName = nameQuery.isEmpty() || (dto.getFirstName() != null && dto.getFirstName().toLowerCase().contains(nameQuery));
            boolean matchesEmail = emailQuery.isEmpty() || (dto.getEmail() != null && dto.getEmail().toLowerCase().contains(emailQuery));
            boolean matchesDept = deptQuery == null || (dto.getDepartment() != null && deptQuery.getDepartmentId() != null && deptQuery.getDepartmentId().equals(dto.getDepartment().getDepartmentId()));
            return matchesName && matchesEmail && matchesDept;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }

    private void updateGrid() {
        loadInitialData();
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
        department.setItemLabelGenerator(item -> item != null ? item.getDepartmentName() : "");

        ComboBox<RoleDTO> role = new ComboBox<>("Role");
        role.setItems(roleService.getAllRoleDTOs());
        role.setItemLabelGenerator(item -> item != null ? item.getRoleName() : "");

        ComboBox<EmployeeDTO> manager = new ComboBox<>("Manager");
        manager.setItems(employeeService.getAllManagerDTOs());
        manager.setItemLabelGenerator(dto -> dto != null ? dto.getFirstName() + " " + dto.getLastName() : "");

        firstName.setWidthFull();
        lastName.setWidthFull();
        email.setWidthFull();
        phone.setWidthFull();
        designation.setWidthFull();
        joiningDate.setWidthFull();
        department.setWidthFull();
        role.setWidthFull();
        manager.setWidthFull();

        Binder<EmployeeDTO> binder = new Binder<>(EmployeeDTO.class);


        binder.forField(firstName).asRequired("First name is required.").withValidator(new StringLengthValidator("First name must be between 2 and 20 characters.", 2, 20)).bind(EmployeeDTO::getFirstName, EmployeeDTO::setFirstName);
        binder.forField(lastName).asRequired("Last name is required.").withValidator(new StringLengthValidator("Last name must be between 1 and 20 characters.", 1, 20)).bind(EmployeeDTO::getLastName, EmployeeDTO::setLastName);
        binder.forField(email).asRequired("Email address is required.").withValidator(new EmailValidator("Please enter a valid email layout format.")).bind(EmployeeDTO::getEmail, EmployeeDTO::setEmail);
        binder.forField(phone).asRequired("Phone number is required.").withValidator(p -> p.matches("^[0-9]{10}$"), "Enter a valid phone number format (10-13 digits).").bind(EmployeeDTO::getPhone, EmployeeDTO::setPhone);
        binder.forField(designation).asRequired("Designation is required.").bind(EmployeeDTO::getDesignation, EmployeeDTO::setDesignation);
        binder.forField(joiningDate).asRequired("Date of Joining must be selected.").withValidator(date -> !date.isAfter(LocalDate.now().plusMonths(1)), "Date of joining cannot be set too far in the future.").bind(EmployeeDTO::getDateOfJoining, EmployeeDTO::setDateOfJoining);
        binder.forField(department).asRequired("Please allocate a department.").bind(EmployeeDTO::getDepartment, EmployeeDTO::setDepartment);
        binder.forField(role).asRequired("An access role assignment is required.").bind(EmployeeDTO::getRole, EmployeeDTO::setRole);
        binder.forField(manager).bind(EmployeeDTO::getManager, EmployeeDTO::setManager);

        role.addValueChangeListener(e -> {
            boolean isManagerRole = e.getValue() != null && e.getValue().getRoleName().equalsIgnoreCase("MANAGER");
            manager.setVisible(!isManagerRole);
            if (isManagerRole) {
                manager.clear();
            }
        });

        EmployeeDTO dtoToSave = (employeeDTO == null) ? new EmployeeDTO() : employeeDTO;
        binder.readBean(dtoToSave);

        Button saveBtn = new Button(employeeDTO == null ? "SAVE" : "UPDATE");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(dtoToSave)) {

                if (employeeDTO == null) {
                    String cleanFirstName = dtoToSave.getFirstName().replaceAll("\\s+", "").toLowerCase();
                    String cleanLastName = dtoToSave.getLastName().replaceAll("\\s+", "").toLowerCase();
                    dtoToSave.setUsername(cleanFirstName + cleanLastName);
                    dtoToSave.setPassword("password123");
                }

                if (employeeDTO == null) {
                    employeeService.registerEmployee(dtoToSave);
                    Notification.show("Employee successfully created!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    employeeService.updateEmployee(dtoToSave);
                    Notification.show("Employee records successfully updated.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                updateGrid();
            } else {
                Notification.show("Enter correct form inputs.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel", click -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        layout.add(firstName, lastName, email, phone, designation, joiningDate, department, role, manager, buttonLayout);

        dialog.add(layout);
        dialog.open();
    }
}