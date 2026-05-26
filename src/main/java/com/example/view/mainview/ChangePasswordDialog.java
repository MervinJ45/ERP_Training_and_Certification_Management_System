package com.example.view.mainview;

import com.example.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class ChangePasswordDialog extends Dialog {

    private final UserService userService;
    private final Long currentUserId;

    private final PasswordField currentPassword = new PasswordField("Current Password");
    private final PasswordField newPassword = new PasswordField("New Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm New Password");

    private final Button saveBtn = new Button("Update Password");
    private final Binder<PasswordFormModel> binder = new Binder<>(PasswordFormModel.class);

    public ChangePasswordDialog(UserService userService, Long currentUserId) {
        this.userService = userService;
        this.currentUserId = currentUserId;

        setHeaderTitle("Security Management: Change Password");
        setWidth("400px");

        VerticalLayout layout = new VerticalLayout(currentPassword, newPassword, confirmPassword);
        layout.setPadding(false);
        layout.setSpacing(true);

        currentPassword.setWidthFull();
        newPassword.setWidthFull();
        confirmPassword.setWidthFull();

        add(layout);

        configureFormValidation();
        configureActions();
    }

    private void configureFormValidation() {
        binder.forField(currentPassword).asRequired("Current password field cannot be blank.").bind(PasswordFormModel::getCurrentPassword, PasswordFormModel::setCurrentPassword);

        binder.forField(newPassword).asRequired("New password selection is required.").withValidator(new StringLengthValidator("Password must be between 8 and 100 characters long.", 8, 100)).withValidator(pass -> pass.matches(".*[0-9].*"), "Password must contain at least one numerical digit.").bind(PasswordFormModel::getNewPassword, PasswordFormModel::setNewPassword);

        binder.forField(confirmPassword).asRequired("Please re-type your chosen password configuration.").withValidator(confirmPass -> confirmPass.equals(newPassword.getValue()), "Confirmation does not match target new password.").bind(PasswordFormModel::getConfirmPassword, PasswordFormModel::setConfirmPassword);
    }

    private void configureActions() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(event -> {
            PasswordFormModel formModel = new PasswordFormModel();

            if (binder.writeBeanIfValid(formModel)) {
                boolean success = userService.updateLoggedInPassword(currentUserId, formModel.getCurrentPassword(), formModel.getNewPassword());

                if (success) {
                    Notification.show("Your password has been changed successfully.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    close();
                } else {
                    currentPassword.setInvalid(true);
                    currentPassword.setErrorMessage("The provided current password is incorrect.");
                    Notification.show("Operation failed. Verify authorization parameters.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Please correct the validation problems highlighted on the form.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel", e -> close());
        getFooter().add(saveBtn, cancelBtn);
    }

    public static class PasswordFormModel {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}