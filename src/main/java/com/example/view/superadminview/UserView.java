package com.example.view.superadminview;

import com.example.dto.UserDTO;
import com.example.service.UserService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "user", layout = MainLayout.class)
@PageTitle("User Management")
@RolesAllowed("SUPER_ADMIN")
public class UserView extends VerticalLayout {

    private final UserService userService;

    private final Grid<UserDTO> grid = new Grid<>(UserDTO.class, false);
    private final TextField filterField = new TextField();

    public UserView(UserService userService) {

        this.userService = userService;

        setSizeFull();
        setSpacing(true);

        H2 title = new H2("User Accounts");

        filterField.setPlaceholder("Filter by username");
        filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.setWidth("350px");

        filterField.addValueChangeListener(e -> updateGrid());

        configureGrid();
        updateGrid();

        Scroller gridScroller = new Scroller(grid);
        gridScroller.setSizeFull();
        gridScroller.setScrollDirection(Scroller.ScrollDirection.BOTH);

        add(title, filterField, gridScroller);
        expand(gridScroller);
    }

    private void configureGrid() {

        grid.setWidthFull();
        grid.setHeight("100%");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(UserDTO::getUsername)
                .setHeader("Username")
                .setWidth("200px")
                .setFlexGrow(0)
                .setFrozen(true);

        grid.addColumn(UserDTO::getEmail)
                .setHeader("Email")
                .setWidth("300px")
                .setFlexGrow(0);

        grid.addColumn(UserDTO::getEmployeeName)
                .setHeader("Linked Employee")
                .setWidth("250px")
                .setFlexGrow(0);

        grid.addColumn(userDTO ->
                        userDTO.getRole() != null
                                ? userDTO.getRole().getRoleName()
                                : "No Role")
                .setHeader("System Role")
                .setWidth("200px")
                .setFlexGrow(0);
    }

    private void updateGrid() {

        String value = filterField.getValue();

        if (value == null || value.isEmpty()) {
            grid.setItems(userService.getAllUserDTOs());
        } else {
            grid.setItems(userService.searchUserDTOs(value));
        }
    }
}