package com.example.view.managerview;

import com.example.entity.CertificationRenewal;
import com.example.entity.User;
import com.example.service.CertificationRenewalService;
import com.example.utils.CurrentUserProvider;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "cert-approvals", layout = MainLayout.class)
@PageTitle("ERP | Certificate Renewal Approvals")
@RolesAllowed({"MANAGER"})
public class CertificateApprovalView extends VerticalLayout {

    private final CertificationRenewalService renewalService;
    private final CurrentUserProvider currentUserProvider;
    private final Grid<CertificationRenewal> grid = new Grid<>();

    public CertificateApprovalView(CertificationRenewalService renewalService, CurrentUserProvider currentUserProvider) {
        this.renewalService = renewalService;
        this.currentUserProvider = currentUserProvider;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Certificate Renewal");

        configureGrid();
        refreshGridData();

        add(title, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(CertificationRenewal::getRenewalId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(renewal -> renewal.getOriginalCertification().getCertificateNumber()).setHeader("Certification Number");
        grid.addColumn(renewal -> renewal.getEmployee().getFirstName() + " " + renewal.getEmployee().getFirstName()).setHeader("Employee Name");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        grid.addColumn(renewal -> renewal.getRenewalDate() != null ? renewal.getRenewalDate().format(formatter) : "N/A").setHeader("Submission Date");

        grid.addColumn(new ComponentRenderer<>(renewal -> {
            Anchor viewLink = new Anchor(renewal.getUploadedCertificateUrl(), "View Certificate");
            viewLink.setTarget("_blank");

            Button viewBtn = new Button();
            viewBtn.setIcon(VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            viewLink.add(viewBtn);
            return viewLink;
        })).setHeader("Uploaded File").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(renewal -> {
            Button acceptBtn = new Button("Accept");
            acceptBtn.setIcon(VaadinIcon.CHECK.create());
            acceptBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            acceptBtn.addClickListener(e -> openDecisionBox(renewal, true));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setIcon(VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            rejectBtn.addClickListener(e -> openDecisionBox(renewal, false));

            HorizontalLayout actions = new HorizontalLayout(acceptBtn, rejectBtn);
            actions.setSpacing(true);
            return actions;
        })).setHeader("Review Decisions").setAutoWidth(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void openDecisionBox(CertificationRenewal renewal, boolean isApproved) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isApproved ? "Confirm Certificate Approval" : "Reject Certificate Document");
        dialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        TextArea remarksField = new TextArea("Decision Remarks");
        remarksField.setPlaceholder("Provide justification notes or feedback ...");
        remarksField.setWidthFull();
        layout.add(remarksField);

        Button confirmBtn = new Button(isApproved ? "Approve & Update Expiry" : "Reject Application", e -> {
            try {
                User contextUser = currentUserProvider.getCurrentUser();
                Long directorEmpId = contextUser.getEmployee().getEmployeeId();

                renewalService.processApprovalDecision(renewal.getRenewalId(), directorEmpId, isApproved, remarksField.getValue());

                Notification n = Notification.show(isApproved ? "Certification validated. Expiry date automatically shifted forward." : "Application rejected successfully.");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                refreshGridData();
            } catch (Exception ex) {
                Notification.show("Failed to write to database: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, isApproved ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR);
        confirmBtn.setWidthFull();

        Button cancelBtn = new Button("Cancel", click -> dialog.close());
        cancelBtn.setWidthFull();

        dialog.add(layout);
        dialog.getFooter().add(confirmBtn, cancelBtn);
        dialog.open();
    }

    private void refreshGridData() {
        List<CertificationRenewal> pendingRequests = renewalService.getPendingRenewals();
        grid.setItems(pendingRequests);
    }
}