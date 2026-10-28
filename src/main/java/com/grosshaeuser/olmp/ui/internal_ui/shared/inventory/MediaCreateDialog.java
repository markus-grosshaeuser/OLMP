package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.ui.internal_ui.shared.CrudCallbacks;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.Objects;

public class MediaCreateDialog extends Dialog implements Structurable, CrudCallbacks {

    private final UiContext uiContext;

    private final MediaForm<MediaCreateDto> form;
    private final CreateCallback<MediaCreateDto> createCallback;

    public MediaCreateDialog(InventoryContext inventoryContext, UiContext uiContext, CreateCallback<MediaCreateDto> createCallback) {
        this.uiContext = Objects.requireNonNull(uiContext, "uiContext");
        this.createCallback = Objects.requireNonNull(createCallback, "createCallback");
        form = new MediaForm<>(MediaCreateDto.class, inventoryContext);
        form.read(new MediaCreateDto());

        getHeader().add(createDialogHeader("Create medium", "Create a new medium", this::close));

        setWidth("64rem");
        setMaxWidth("95vw");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        add(form);
        Button save = new Button("Save", _ -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);
        Button cancel = new Button("Cancel", _ -> close());
        getFooter().add(createActionBar(new Button[]{save}, new Button[]{}, new Button[]{cancel}));
    }

    private void save() {
        try {
            MediaCreateDto submission = new MediaCreateDto();
            if (form.write(submission)) {
                createCallback.create(submission);
                close();
            }
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        }
    }
}
