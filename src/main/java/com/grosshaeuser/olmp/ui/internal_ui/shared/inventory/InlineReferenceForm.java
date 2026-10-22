package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormSupport.responsiveForm;

class InlineReferenceForm<D> extends VerticalLayout {
    final BeanValidationBinder<D> binder;
    private final Supplier<D> factory;
    private final Span error = new Span();
    private final TextField firstField;
    private D draft;
    private boolean editing;

    InlineReferenceForm(Class<D> type, Supplier<D> factory, Consumer<D> saveRecord, String label, TextField... fields) {
        this.binder = new BeanValidationBinder<>(type);
        this.factory = factory;
        firstField = fields[0];
        setPadding(true);
        setWidthFull();
        addClassName("inline-create-form");
        FormLayout inputs = responsiveForm();
        for (TextField field : fields) {
            field.setWidthFull();
            inputs.add(field);
        }
        error.getElement().setAttribute("role", "alert");
        error.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        Button save = new Button("Save " + label, _ -> {
            error.setVisible(false);
            if (!binder.writeBeanIfValid(draft)) {
                return;
            }
            try {
                saveRecord.accept(draft);
                reset();
            } catch (RuntimeException exception) {
                error.setText("Could not " + (editing ? "update " : "create ") + label + ". "
                        + Objects.requireNonNullElse(exception.getMessage(), "Please try again."));
                error.setVisible(true);
            }
        });
        save.addThemeVariants(ButtonVariant.PRIMARY);
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.addToStart(save);
        actions.addToEnd(new Button("Cancel", _ -> reset()));
        add(inputs, actions, error);
    }

    void open() {
        if (editing) {
            reset();
        }
        setVisible(true);
        firstField.focus();
    }

    void openForEdit(D dto) {
        draft = dto;
        editing = true;
        binder.readBean(draft);
        error.setVisible(false);
        setVisible(true);
        firstField.focus();
    }

    void reset() {
        draft = factory.get();
        editing = false;
        binder.readBean(draft);
        error.setVisible(false);
        setVisible(false);
    }
}
