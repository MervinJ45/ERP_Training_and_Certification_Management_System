package com.example.view.component;

import com.example.dto.ApprovalWorkflowConfigDTO;
import com.example.service.ApprovalWorkflowConfigService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkflowInfoDialog extends Dialog {

    private final VerticalLayout containerLayout = new VerticalLayout();

    public WorkflowInfoDialog(ApprovalWorkflowConfigService configService) {
        setHeaderTitle("Approval Tiers");
        setWidth("520px");
        setHeight("580px");
        setCloseOnOutsideClick(true);

        containerLayout.setPadding(true);
        containerLayout.setSpacing(true);
        containerLayout.getStyle().set("overflow-y", "auto");

        List<ApprovalWorkflowConfigDTO> activeConfigs = configService.getAllConfigsAsDTOs().stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .collect(Collectors.toList());

        if (activeConfigs.isEmpty()) {
            Span emptyState = new Span("No active approval matrix rules found.");
            emptyState.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            containerLayout.add(emptyState);
        } else {
            Map<String, List<ApprovalWorkflowConfigDTO>> groupedTiers = activeConfigs.stream()
                    .collect(Collectors.groupingBy(item -> String.format("₹%,d - ₹%,d",
                            item.getMinCost() != null ? item.getMinCost().longValue() : 0L,
                            item.getMaxCost() != null ? item.getMaxCost().longValue() : 0L
                    )));

            int tierIndex = 0;
            for (Map.Entry<String, List<ApprovalWorkflowConfigDTO>> entry : groupedTiers.entrySet()) {
                String priceRange = entry.getKey();
                List<ApprovalWorkflowConfigDTO> levelRules = entry.getValue();

                levelRules.sort(Comparator.comparingInt(a -> a.getRequiredLevel() != null ? a.getRequiredLevel() : 0));

                int totalLevelsForThisRange = levelRules.size();

                H3 rangeTitle = new H3("Tier Range: " + priceRange);
                rangeTitle.getStyle().set("margin", "5px 0 0 0").set("font-size", "var(--lumo-font-size-m)");

                Div box = new Div();
                box.setWidthFull();
                box.getStyle()
                        .set("background-color", "var(--lumo-contrast-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("padding", "14px")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("gap", "10px");

                box.add(createDataRow("Total Required Steps:", totalLevelsForThisRange + " Levels"));

                Div nestedStepsContainer = new Div();
                nestedStepsContainer.getStyle()
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("gap", "6px")
                        .set("margin-top", "4px")
                        .set("padding-left", "8px")
                        .set("border-left", "2px solid var(--lumo-contrast-20pct)");

                for (ApprovalWorkflowConfigDTO rule : levelRules) {
                    String levelLabel = "↳ Level " + rule.getRequiredLevel() + " Approver:";
                    String approverRole = rule.getApproverRoleName() != null ? rule.getApproverRoleName() : "N/A";
                    nestedStepsContainer.add(createDataRow(levelLabel, approverRole));
                }

                box.add(nestedStepsContainer);
                containerLayout.add(rangeTitle, box);

                if (tierIndex < groupedTiers.size() - 1) {
                    Hr separator = new Hr();
                    separator.getStyle().set("margin", "8px 0").set("opacity", "0.3");
                    containerLayout.add(separator);
                }
                tierIndex++;
            }
        }

        add(containerLayout);

        Button closeButton = new Button("Close", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        getFooter().add(closeButton);
    }

    private HorizontalLayout createDataRow(String labelText, String valueText) {
        Span label = new Span(labelText);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        if (labelText.startsWith("↳")) {
            label.getStyle().set("font-weight", "500");
        }

        Span value = new Span(valueText);
        value.getStyle().set("font-size", "var(--lumo-font-size-s)").set("font-weight", "600");

        HorizontalLayout row = new HorizontalLayout(label, value);
        row.setWidthFull();
        row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        return row;
    }
}