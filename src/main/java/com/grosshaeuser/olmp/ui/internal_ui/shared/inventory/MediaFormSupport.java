package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

final class MediaFormSupport {

    private MediaFormSupport() {
    }

    static <R> Button editButton(ComboBox<R> selection, String label, Consumer<R> edit) {
        Button button = new Button(VaadinIcon.EDIT.create(), _ -> {
            if (selection.getValue() != null) {
                edit.accept(selection.getValue());
            }
        });
        button.setTooltipText("Edit " + label);
        button.setAriaLabel("Edit " + label);
        button.setEnabled(selection.getValue() != null);
        selection.addValueChangeListener(event -> button.setEnabled(event.getValue() != null));
        return button;
    }

    static VerticalLayout referenceSection(Component selection, Button edit, Button create, Component form) {
        HorizontalLayout selector = new HorizontalLayout(selection, edit, create);
        selector.setAlignItems(FlexComponent.Alignment.BASELINE);
        selector.setWidthFull();
        selector.setFlexGrow(1, selection);
        VerticalLayout section = new VerticalLayout(selector, form);
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        return section;
    }

    static FormLayout responsiveForm() {
        FormLayout layout = new FormLayout();
        layout.setWidthFull();
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40rem", 2)
        );
        return layout;
    }

    static <R> R lookup(List<R> options, Function<R, Long> idGetter, Long id, String label) {
        if (id == null) {
            return null;
        }
        return options.stream().filter(option -> id.equals(idGetter.apply(option))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(label + " is no longer available. Reopen the form."));
    }

}
