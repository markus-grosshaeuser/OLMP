package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Delegatable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.service.BarcodeService;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Objects;

public class MediaEditComponent extends Composite<VerticalLayout> implements Structurable, Delegatable {

    private final MediaForm<MediaEditDto> form;
    private final UpdateCallback<MediaEditDto> updateCallback;
    private final ErrorCallback errorCallback;
    private MediaEditDto savedMedia;

    public MediaEditComponent(
            MediaEditDto media,
            MediaFormOptions options,
            BarcodeService barcodeService,
            BarcodePrintService barcodePrintService,
            MediaReferenceService referenceService,
            UpdateCallback<MediaEditDto> updateCallback,
            Runnable closeCallback,
            ErrorCallback errorCallback
    ) {
        this.savedMedia = Objects.requireNonNull(media, "media");
        Objects.requireNonNull(media.getId(), "media.id");
        this.updateCallback = Objects.requireNonNull(updateCallback, "updateCallback");
        this.errorCallback = Objects.requireNonNull(errorCallback, "errorCallback");
        Objects.requireNonNull(closeCallback, "closeCallback");
        form = new MediaForm<>(MediaEditDto.class, options, barcodeService, barcodePrintService, referenceService);
        form.read(media);
        Button save = new Button("Save changes", _ -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);
        Button reset = new Button("Reset", _ -> form.read(savedMedia));
        Button close = new Button("Close", _ -> closeCallback.run());
        getContent().setPadding(false);
        getContent().setWidthFull();
        addClassName("media-edit-component");
        getContent().add(createComponentHeader("Edit media", "Update catalogue information, authors, identifiers and physical copies."),
                form, createActionBar(new Button[]{save}, new Button[]{reset}, new Button[]{close}));
    }

    private void save() {
        try {
            MediaEditDto submission = new MediaEditDto();
            submission.setId(savedMedia.getId());
            if (!form.write(submission)) {
                return;
            }
            MediaEditDto updated = Objects.requireNonNull(updateCallback.update(submission), "Saved media");
            if (!savedMedia.getId().equals(updated.getId())) {
                throw new IllegalArgumentException("The saved media ID does not match the edited media.");
            }
            savedMedia = updated;
            form.read(updated);
        } catch (RuntimeException exception) {
            errorCallback.handleException(exception);
        }
    }
}
