package com.example.view.auditorview;

import com.example.dto.CertificationDisplayDTO;
import com.example.service.CertificationService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("ERP | Certification Expiry Report")
@Route(value = "reports/certification-expiry", layout = MainLayout.class)
@RolesAllowed("AUDITOR")
public class CertificationExpiryReportView extends VerticalLayout {

    public CertificationExpiryReportView(CertificationService certificationService) {
        setSizeFull();
        setPadding(true);

        H2 header = new H2("Certification Expiry Report");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        add(header);

        Grid<CertificationDisplayDTO> grid = new Grid<>();
        grid.setSizeFull();
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        grid.addColumn(CertificationDisplayDTO::getEmployee).setHeader("Employee").setAutoWidth(true).setSortable(true);
        grid.addColumn(CertificationDisplayDTO::getCourseName).setHeader("Credential / Course").setAutoWidth(true).setSortable(true);
        grid.addColumn(cert -> cert.getDaysRemaining() != null && cert.getDaysRemaining() > 0 ? cert.getDaysRemaining() + " Days" : "Expired").setHeader("Remaining Timeframe").setAutoWidth(true).setSortable(true);

        grid.addComponentColumn(cert -> {
            Span badge = new Span();
            String status = cert.getStatusName() != null ? cert.getStatusName().trim().toUpperCase() : "UNKNOWN";
            badge.setText(cert.getStatusName() != null ? cert.getStatusName() : "Unknown");

            badge.getElement().getThemeList().add("badge");

            switch (status) {
                case "ACTIVE":
                    badge.getElement().getThemeList().add("success");
                    break;
                case "EXPIRED":
                    badge.getElement().getThemeList().add("error");
                    break;
                case "RENEWED":
                    badge.getStyle().set("background-color", "#e0e7ff");
                    badge.getStyle().set("color", "#4338ca");
                    break;
                default:
                    badge.getElement().getThemeList().add("contrast");
                    break;
            }
            return badge;
        }).setHeader("Operational Status").setAutoWidth(true).setSortable(true);

        grid.setItems(certificationService.getAllCertificationDTOs());
        add(grid);
        setFlexGrow(1, grid);
    }
}