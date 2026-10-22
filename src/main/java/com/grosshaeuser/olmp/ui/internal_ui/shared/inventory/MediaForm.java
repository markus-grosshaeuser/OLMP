package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorWriteDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierWriteDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.InventoryNumberService;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.service.BarcodeService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MediaForm<T extends MediaCreateDto> extends Composite<VerticalLayout> {

    private final MediaCatalogueForm<T> catalogue;
    private final MediaAuthorsForm authors;
    private final MediaIdentifiersForm identifiers;
    private final MediaInventoryForm inventory;

    MediaForm(
            Class<T> dtoType,
            MediaFormOptions options,
            InventoryNumberService inventoryNumberService,
            BarcodeService barcodeService,
            BarcodePrintService barcodePrintService,
            MediaReferenceService referenceService
    ) {
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(barcodeService, "barcodeService");
        catalogue = new MediaCatalogueForm<>(dtoType, options.mediaTypes(), options.publishers(), referenceService);
        authors = new MediaAuthorsForm(options.authors(), referenceService);
        identifiers = new MediaIdentifiersForm();
        inventory = new MediaInventoryForm(options.locations(), inventoryNumberService, barcodePrintService, catalogue::title);

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        sheet.add("Catalogue", catalogue);
        sheet.add("Authors and contributors", authors);
        sheet.add("Identifiers", identifiers);
        sheet.add("Inventory items", inventory);

        getContent().setPadding(false);
        getContent().setWidthFull();
        getContent().add(sheet);
    }

    void read(T source) {
        Objects.requireNonNull(source, "source");
        catalogue.read(source);
        authors.read(source.getAuthors());
        identifiers.read(source.getIdentifiers());
        inventory.read(source.getInventoryItems());
    }

    boolean write(T target) {
        List<MediaAuthorWriteDto> authorDtos = new ArrayList<>();
        List<MediaIdentifierWriteDto> identifierDtos = new ArrayList<>();
        List<MediaInventoryItemDto> inventoryDtos = new ArrayList<>();

        boolean valid = authors.write(authorDtos);
        valid = identifiers.write(identifierDtos) && valid;
        valid = inventory.write(inventoryDtos) && valid;
        valid = catalogue.write(target) && valid;
        if (!valid) {
            return false;
        }

        target.setAuthors(authorDtos);
        target.setIdentifiers(identifierDtos);
        target.setInventoryItems(inventoryDtos);
        return true;
    }
}
