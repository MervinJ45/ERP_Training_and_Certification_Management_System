package com.example.view;

import com.example.dto.UserDTO;
import com.example.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "user", layout = MainLayout.class)
@PageTitle("User Management")
@RolesAllowed("SUPERADMIN")
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final Grid<UserDTO> grid = new Grid<>(UserDTO.class, false);
    private final TextField filterField = new TextField();

    public UserView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setSpacing(true);

        H2 title = new H2("User Accounts");

        filterField.setPlaceholder("Filter by username or email...");
        filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.setWidth("350px");
        filterField.addValueChangeListener(e -> updateGrid());

        configureGrid();
        updateGrid();

        add(title, filterField, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(UserDTO::getUsername)
                .setHeader("Username")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(UserDTO::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        grid.addColumn(userDTO -> userDTO.getEmployee() != null ?
                        userDTO.getEmployee().getFirstName() + " " + userDTO.getEmployee().getLastName() : "SUPER ADMIN")
                .setHeader("Linked Employee")
                .setAutoWidth(true);

        grid.addColumn(userDTO -> userDTO.getRole() != null ? userDTO.getRole().getRoleName() : "No Role")
                .setHeader("System Role")
                .setAutoWidth(true);

    }

    private void updateGrid() {
        String value = filterField.getValue();
        if (value == null || value.isEmpty()) {
            grid.setItems(userService.getAllUserDTOs());
        }
        //TO DO : filter
    }
}