package com.example.view.mainview;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | ERP System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Login");

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.setWidth("420px");
        card.setPadding(true);
        card.getStyle().set("background-color", "white");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 title = new H2("Welcome Back");

        username.getElement().setAttribute("name", "username");
        username.setWidthFull();

        password.getElement().setAttribute("name", "password");
        password.setWidthFull();

        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

        loginButton.getElement().executeJs(
                "this.addEventListener('click', () => { this.closest('form').submit(); });"
        );

        HtmlComponent form = new HtmlComponent("form");
        form.getElement().setAttribute("method", "post");
        form.getElement().setAttribute("action", "login");
        form.getElement().getStyle().set("width", "100%");

        VerticalLayout formLayout = new VerticalLayout(username, password, loginButton);
        form.getElement().appendChild(formLayout.getElement());

        card.add(title, form);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            Notification.show("Invalid credentials", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}