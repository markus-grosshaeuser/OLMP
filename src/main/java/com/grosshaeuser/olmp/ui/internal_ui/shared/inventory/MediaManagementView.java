package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.service.MediaLookupService;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import com.grosshaeuser.olmp.inventory.service.BarcodeService;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaSummaryDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediaManagementView extends VerticalLayout {
    private final MediaManagementService mediaService;
    private final MediaLookupService lookupService;
    private final BarcodeService barcodeService;
    private final BarcodePrintService barcodePrintService;
    private final UiExceptionHandler uiExceptionHandler;
    private final UiNotificationHandler uiNotificationHandler;
    private final TextField searchField = new TextField("Search media");
    private final Grid<MediaSummaryDto> mediaGrid = new Grid<>(MediaSummaryDto.class, false);

    public MediaManagementView(
            MediaManagementService mediaService,
            MediaLookupService lookupService,
            BarcodeService barcodeService,
            BarcodePrintService barcodePrintService,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        this.mediaService = Objects.requireNonNull(mediaService, "mediaService");
        this.lookupService = Objects.requireNonNull(lookupService, "lookupService");
        this.barcodeService = Objects.requireNonNull(barcodeService, "barcodeService");
        this.barcodePrintService = Objects.requireNonNull(barcodePrintService, "barcodeLabelService");
        this.uiExceptionHandler = Objects.requireNonNull(uiExceptionHandler, "uiExceptionHandler");
        this.uiNotificationHandler = Objects.requireNonNull(uiNotificationHandler, "uiNotificationHandler");
        configureSearch();
        configureGrid();

        Button create = new Button("Create media", VaadinIcon.PLUS.create(), _ -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(searchField, create);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        toolbar.setWrap(true);
        add(new H2("Media management"), toolbar, mediaGrid);
        setSizeFull();
        setFlexGrow(1, mediaGrid);
    }

    private void configureSearch() {
        searchField.setPlaceholder("Search by title or subtitle");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(_ -> refreshGrid());
    }

    private void configureGrid() {
        mediaGrid.addColumn(MediaSummaryDto::title)
                .setHeader("Title")
                .setKey("title")
                .setSortProperty("title")
                .setFlexGrow(3)
                .setResizable(true);
        mediaGrid.addColumn(media -> media.authors()
                        .stream()
                        .map(author -> MediaForm.authorName(author.author()) + " (" + author.role() + ")")
                        .collect(Collectors.joining(", "))
                )
                .setHeader("Authors / contributors").setFlexGrow(2).setResizable(true);
        mediaGrid.addColumn(media -> media.mediaType().name())
                .setHeader("Type")
                .setSortProperty("mediaType.name")
                .setAutoWidth(true)
                .setFlexGrow(0);
        mediaGrid.addColumn(media -> media.publisher() == null ? "" : media.publisher().name())
                .setHeader("Publisher")
                .setSortProperty("publisher.name")
                .setResizable(true);
        mediaGrid.addColumn(MediaSummaryDto::publicationYear)
                .setHeader("Year")
                .setSortProperty("publicationYear")
                .setAutoWidth(true)
                .setFlexGrow(0);
        mediaGrid.addComponentColumn(media -> {
            Button edit = new Button(VaadinIcon.EDIT.create(), _ -> mediaGrid.setDetailsVisible(media, !mediaGrid.isDetailsVisible(media)));
            edit.setAriaLabel("Edit " + media.title());
            edit.addThemeVariants(ButtonVariant.LUMO_ICON);
            Button delete = new Button(VaadinIcon.TRASH.create(), _ -> confirmDelete(media));
            delete.setAriaLabel("Delete " + media.title());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            Span actionElements = new Span(edit, delete);
            actionElements.addClassName("grid-action-elements");
            return actionElements;
        })
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        mediaGrid.setSelectionMode(Grid.SelectionMode.NONE);
        mediaGrid.setDetailsVisibleOnClick(false);
        mediaGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        mediaGrid.addThemeVariants(GridVariant.ROW_STRIPES, GridVariant.WRAP_CELL_CONTENT);
        mediaGrid.setSizeFull();
        mediaGrid.setItemsPageable(this::fetchMedia, _ -> countMedia()).setIdentifierProvider(MediaSummaryDto::id);
        mediaGrid.sort(List.of(new GridSortOrder<>(mediaGrid.getColumnByKey("title"), SortDirection.ASCENDING)));
        mediaGrid.setEmptyStateText("No media found");
    }

    private List<MediaSummaryDto> fetchMedia(Pageable pageable) {
        try {
            return mediaService.findMedia(searchField.getValue().strip(), pageable);
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return List.of();
        }
    }

    private int countMedia() {
        try {
            return Math.toIntExact(mediaService.countMedia(searchField.getValue().strip()));
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return 0;
        }
    }

    private Component createEditor(MediaSummaryDto media) {
        try {
            return mediaService.findMediaWithInventoryForEdit(media.id())
                    .<Component>map(dto -> new MediaEditComponent(
                            dto,
                            lookupService.getFormOptions(),
                            barcodeService,
                            barcodePrintService,
                            changed -> updateMedia(changed, media),
                            () -> mediaGrid.setDetailsVisible(media, false),
                            uiExceptionHandler::handleException)
                    )
                    .orElseGet(() -> unavailableDetails("This media no longer exists."));
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return unavailableDetails("Media details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void openCreateDialog() {
        try {
            new MediaCreateDialog(
                    lookupService.getFormOptions(),
                    barcodeService,
                    this::createMedia,
                    uiExceptionHandler::handleException
            ).open();
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
        }
    }

    private void createMedia(MediaCreateDto dto) {
        List<MediaInventoryItemDto> newCopies = newlyAddedCopies(dto);
        Objects.requireNonNull(mediaService.createMediaWithInventory(dto), "Media creation failed");
        uiNotificationHandler.showSuccessNotification("Media created successfully.");
        if (searchField.isEmpty()) {
            refreshGrid();
        } else {
            searchField.clear();
        }
        offerBarcodePrinting(dto.getTitle(), newCopies);
    }

    private MediaEditDto updateMedia(MediaEditDto dto, MediaSummaryDto summary) {
        List<MediaInventoryItemDto> newCopies = newlyAddedCopies(dto);
        MediaEditDto updated = Objects.requireNonNull(mediaService.updateMediaWithInventory(dto), "Updated media");
        mediaGrid.setDetailsVisible(summary, false);
        mediaGrid.getDataProvider().refreshAll();
        uiNotificationHandler.showSuccessNotification("Media updated successfully.");
        offerBarcodePrinting(updated.getTitle(), newCopies);
        return updated;
    }

    private static List<MediaInventoryItemDto> newlyAddedCopies(MediaCreateDto dto) {
        return dto.getInventoryItems().stream().filter(copy -> copy.id() == null).toList();
    }

    private void offerBarcodePrinting(String title, List<MediaInventoryItemDto> copies) {
        if (!copies.isEmpty()) {
            new BarcodePrintDialog(barcodePrintService, title, copies).open();
        }
    }

    private void confirmDelete(MediaSummaryDto medium) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.addClassName("confirm-delete-dialog");
        dialog.setHeader("Delete Medium?");
        dialog.setText(new Html("<p>You are about to delete the medium:<br /><em>" + medium.title() + "</em><br />This action cannot be undone!</p>"));
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(_ -> delete(medium));
        dialog.open();
    }

    private void delete(MediaSummaryDto medium) {
        mediaService.deleteMedium(medium.id());
        refreshGrid();
    }

    private void refreshGrid() {
        mediaGrid.getDataProvider().refreshAll();
        mediaGrid.scrollToIndex(0);
    }
}
