package com.example.view.auditorview;

import com.example.entity.AuditLog;
import com.example.service.AuditLogService;
import com.example.view.mainview.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("ERP | Audit History Report")
@Route(value = "reports/audit-history", layout = MainLayout.class)
@RolesAllowed("AUDITOR")
public class AuditHistoryReportView extends VerticalLayout {

    private final AuditLogService auditLogService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Grid<AuditLog> grid = new Grid<>();

    private final TextField triggerAgentFilter = new TextField();
    private final ComboBox<String> actionFilter = new ComboBox<>();
    private final ComboBox<String> tableFilter = new ComboBox<>();

    private List<AuditLog> allAuditLogs;

    public AuditHistoryReportView(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;

        setSizeFull();
        setPadding(true);

        H2 header = new H2("Audit History Report");
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        configureGrid();

        this.allAuditLogs = auditLogService.getAllAuditLogs();

        configureFilters();

        HorizontalLayout spacer = new HorizontalLayout();
        HorizontalLayout toolbar = new HorizontalLayout(triggerAgentFilter, actionFilter, tableFilter, spacer);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.expand(spacer);

        Scroller gridScroller = new Scroller(grid);
        gridScroller.setSizeFull();
        gridScroller.setScrollDirection(Scroller.ScrollDirection.BOTH);

        add(header, toolbar, gridScroller);
        setFlexGrow(1, gridScroller);

        filterGrid();
    }

    private void configureFilters() {
        triggerAgentFilter.setPlaceholder("Filter by Agent Username");
        triggerAgentFilter.setClearButtonVisible(true);
        triggerAgentFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        triggerAgentFilter.setWidth("240px");
        triggerAgentFilter.setValueChangeMode(ValueChangeMode.LAZY);
        triggerAgentFilter.addValueChangeListener(e -> filterGrid());

        List<String> distinctActions = allAuditLogs.stream()
                .map(AuditLog::getAction)
                .filter(action -> action != null && !action.isBlank())
                .distinct()
                .collect(Collectors.toList());

        actionFilter.setPlaceholder("Operation Action (All)");
        actionFilter.setClearButtonVisible(true);
        actionFilter.setWidth("220px");
        actionFilter.setItems(distinctActions);
        actionFilter.addValueChangeListener(e -> filterGrid());

        List<String> distinctTables = allAuditLogs.stream()
                .map(AuditLog::getTableAffected)
                .filter(table -> table != null && !table.isBlank())
                .distinct()
                .collect(Collectors.toList());

        tableFilter.setPlaceholder("Target Catalog Table (All)");
        tableFilter.setClearButtonVisible(true);
        tableFilter.setWidth("240px");
        tableFilter.setItems(distinctTables);
        tableFilter.addValueChangeListener(e -> filterGrid());
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        grid.addColumn(log -> log.getActionTime() != null ? log.getActionTime().format(dateTimeFormatter) : "N/A")
                .setHeader("Timestamp").setFlexGrow(0).setSortable(true).setAutoWidth(true);

        grid.addColumn(log -> log.getUser() != null ? log.getUser().getUsername() : "SYSTEM")
                .setHeader("Trigger Agent").setFlexGrow(0).setSortable(true).setAutoWidth(true);

        grid.addColumn(AuditLog::getAction)
                .setHeader("Operation Action").setFlexGrow(0).setSortable(true).setAutoWidth(true);

        grid.addColumn(AuditLog::getTableAffected)
                .setHeader("Target Catalog Table").setSortable(true).setAutoWidth(true);

        grid.addColumn(AuditLog::getChangeDetails)
                .setHeader("Mutation Log Details").setAutoWidth(true);
    }

    private void filterGrid() {
        if (allAuditLogs == null) return;

        String agentQuery = triggerAgentFilter.getValue() != null ? triggerAgentFilter.getValue().trim().toLowerCase() : "";
        String actionQuery = actionFilter.getValue();
        String tableQuery = tableFilter.getValue();

        if (agentQuery.isEmpty() && actionQuery == null && tableQuery == null) {
            grid.setItems(allAuditLogs);
            return;
        }

        List<AuditLog> filteredList = allAuditLogs.stream().filter(log -> {
            String operationalUsername = (log.getUser() != null && log.getUser().getUsername() != null)
                    ? log.getUser().getUsername().toLowerCase()
                    : "system";

            boolean matchesAgent = agentQuery.isEmpty() || operationalUsername.contains(agentQuery);
            boolean matchesAction = actionQuery == null || (log.getAction() != null && log.getAction().equals(actionQuery));
            boolean matchesTable = tableQuery == null || (log.getTableAffected() != null && log.getTableAffected().equals(tableQuery));

            return matchesAgent && matchesAction && matchesTable;
        }).collect(Collectors.toList());

        grid.setItems(filteredList);
    }
}