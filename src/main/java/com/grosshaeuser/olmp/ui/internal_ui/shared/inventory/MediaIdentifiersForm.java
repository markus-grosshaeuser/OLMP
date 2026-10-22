package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierWriteDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

class MediaIdentifiersForm extends Composite<Div> implements Structurable {

    private final VerticalLayout identifierRows = new VerticalLayout();
    private final List<IdentifierRow> identifiers = new ArrayList<>();
    private final Span validationMessage = new Span();

    MediaIdentifiersForm() {
        identifierRows.setPadding(false);
        Button addIdentifier = new Button("Add identifier", _ -> addIdentifier(new MediaIdentifierWriteDto()));
        validationMessage.getElement().setAttribute("role", "alert");
        validationMessage.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        validationMessage.setVisible(false);
        getContent().add(createSection("Identifiers", "Add identifiers such as ISBN, ISSN or DOI.",
                identifierRows, addIdentifier, validationMessage));
        getContent().setWidthFull();
    }

    void read(List<MediaIdentifierWriteDto> source) {
        identifiers.clear();
        identifierRows.removeAll();
        source.forEach(this::addIdentifier);
        validationMessage.setVisible(false);
    }

    boolean write(List<MediaIdentifierWriteDto> target) {
        validationMessage.setVisible(false);
        List<MediaIdentifierWriteDto> values = new ArrayList<>();
        boolean valid = true;
        for (IdentifierRow row : identifiers) {
            MediaIdentifierWriteDto dto = new MediaIdentifierWriteDto();
            dto.setId(row.id);
            valid = row.binder.writeBeanIfValid(dto) && valid;
            values.add(dto);
        }
        if (!valid) {
            return false;
        }
        if (hasDuplicateIdentifiers(values)) {
            showValidationMessage("Each identifier type and value combination must be unique.");
            return false;
        }
        target.addAll(values);
        return true;
    }

    private void addIdentifier(MediaIdentifierWriteDto dto) {
        IdentifierRow row = new IdentifierRow(dto);
        identifiers.add(row);
        identifierRows.add(row.layout);
    }

    private void showValidationMessage(String message) {
        validationMessage.setText(message);
        validationMessage.setVisible(true);
    }

    private static boolean hasDuplicateIdentifiers(List<MediaIdentifierWriteDto> rows) {
        record Key(String type, String value) {
        }
        var seen = new HashSet<Key>();
        return rows.stream().anyMatch(row -> !seen.add(new Key(row.getIdentifierType(), row.getIdentifierValue())));
    }

    private class IdentifierRow {
        private final BeanValidationBinder<MediaIdentifierWriteDto> binder = new BeanValidationBinder<>(MediaIdentifierWriteDto.class);
        private final HorizontalLayout layout = new HorizontalLayout();
        private final Long id;

        IdentifierRow(MediaIdentifierWriteDto dto) {
            id = dto.getId();
            TextField type = new TextField("Identifier type");
            type.setMaxLength(30);
            type.setPlaceholder("ISBN, ISSN, ...");

            TextField value = new TextField("Identifier value");
            value.setMaxLength(100);

            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                identifiers.remove(this);
                identifierRows.remove(layout);
            });
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            remove.setAriaLabel("Remove identifier");
            binder.forField(type).asRequired("Identifier type is required")
                    .withConverter(text -> text.strip().toUpperCase(Locale.ROOT), text -> text == null ? "" : text)
                    .bind("identifierType");
            binder.forField(value).asRequired("Identifier value is required")
                    .withConverter(String::strip, text -> text == null ? "" : text).bind("identifierValue");

            List.<HasSize>of(type, value).forEach(HasSize::setWidthFull);
            layout.setSizeFull();
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.add(type, value, remove);
            layout.setFlexGrow(1, type);
            layout.setFlexGrow(1, value);
            binder.readBean(dto);
        }
    }
}
