package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.service.*;
import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.ConfirmDelete;
import com.grosshaeuser.olmp.ui.internal_ui.shared.GridSupport;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaSummaryDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
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

public class MediaManagementView extends VerticalLayout implements ConfirmDelete {
    private final InventoryContext inventoryContext;
    private final UiContext uiContext;

    private final TextField searchField = new TextField("Search media");
    private final ComboBox<AuthorSummaryDto> authorFilter = new ComboBox<>("Author");
    private final ComboBox<PublisherSummaryDto> publisherFilter = new ComboBox<>("Publisher");
    private final Grid<MediaSummaryDto> mediaGrid = GridSupport.getPreconfiguredManagementGrid(MediaSummaryDto.class, "No media found");

    public MediaManagementView(InventoryContext inventoryContext, UiContext uiContext) {
        this.inventoryContext = Objects.requireNonNull(inventoryContext, "inventoryContext");
        this.uiContext = Objects.requireNonNull(uiContext, "uiContext");

        configureSearch();
        configureFilters();
        configureGrid();

        Button createMedia = new Button("Create media", VaadinIcon.PLUS.create(), _ -> openCreateDialog());
        createMedia.addThemeVariants(ButtonVariant.PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(searchField, authorFilter, publisherFilter, createMedia);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(2, searchField);
        toolbar.setFlexGrow(1, authorFilter, publisherFilter);
        for (Component field : List.of(searchField, authorFilter, publisherFilter)) {
            field.getElement().getStyle().set("flex-basis", "0").set("min-width", "0");
        }
        toolbar.setFlexShrink(0, createMedia);
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

    private void configureFilters() {
        authorFilter.setItemLabelGenerator(MediaAuthorsForm::authorName);
        authorFilter.setPlaceholder("All authors");
        authorFilter.setClearButtonVisible(true);
        publisherFilter.setItemLabelGenerator(PublisherSummaryDto::name);
        publisherFilter.setPlaceholder("All publishers");
        publisherFilter.setClearButtonVisible(true);
        try {
            MediaFormOptions options = inventoryContext.getMediaLookupService().getFormOptions();
            authorFilter.setItems(options.authors());
            publisherFilter.setItems(options.publishers());
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        }
        authorFilter.addValueChangeListener(_ -> refreshGrid());
        publisherFilter.addValueChangeListener(_ -> refreshGrid());
    }

    private Long selectedAuthorId() {
        return authorFilter.getValue() == null ? null : authorFilter.getValue().id();
    }

    private Long selectedPublisherId() {
        return publisherFilter.getValue() == null ? null : publisherFilter.getValue().id();
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
                        .map(author -> MediaAuthorsForm.authorName(author.author()) + " (" + author.role() + ")")
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
        mediaGrid.addComponentColumn(media -> GridSupport
                        .createGridActionElements(
                                media.title(),
                                mediaGrid,
                                media,
                                _ -> confirmDelete(media)
                        )
                )
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        mediaGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        mediaGrid.setItemsPageable(this::fetchMedia, _ -> countMedia()).setIdentifierProvider(MediaSummaryDto::id);
        mediaGrid.sort(List.of(new GridSortOrder<>(mediaGrid.getColumnByKey("title"), SortDirection.ASCENDING)));
    }

    private List<MediaSummaryDto> fetchMedia(Pageable pageable) {
        try {
            return inventoryContext.getMediaManagementService().findMedia(searchField.getValue().strip(), selectedAuthorId(), selectedPublisherId(), pageable);
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return List.of();
        }
    }

    private int countMedia() {
        try {
            return Math.toIntExact(inventoryContext.getMediaManagementService().countMedia(searchField.getValue().strip(), selectedAuthorId(), selectedPublisherId()));
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return 0;
        }
    }

    private Component createEditor(MediaSummaryDto media) {
        try {
            return inventoryContext.getMediaManagementService().findMediaWithInventoryForEdit(media.id())
                    .<Component>map(dto -> new MediaEditComponent(
                            dto,
                            inventoryContext,
                            uiContext,
                            changed -> updateMedia(changed, media),
                            () -> mediaGrid.setDetailsVisible(media, false)
                            )
                    )
                    .orElseGet(() -> unavailableDetails("This media no longer exists."));
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return unavailableDetails("Media details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void openCreateDialog() {
        try {
            new MediaCreateDialog(
                    inventoryContext,
                    uiContext,
                    this::createMedia
            ).open();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        }
    }

    private void createMedia(MediaCreateDto dto) {
        List<MediaInventoryItemDto> newCopies = newlyAddedCopies(dto);
        Objects.requireNonNull(inventoryContext.getMediaManagementService().createMediaWithInventory(dto), "Media creation failed");
        uiContext.showSuccessNotification("Media created successfully.");
        searchField.setValue(dto.getTitle());
        offerBarcodePrinting(dto.getTitle(), newCopies);
    }

    private MediaEditDto updateMedia(MediaEditDto dto, MediaSummaryDto summary) {
        List<MediaInventoryItemDto> newCopies = newlyAddedCopies(dto);
        MediaEditDto updated = Objects.requireNonNull(inventoryContext.getMediaManagementService().updateMediaWithInventory(dto), "Updated media");
        mediaGrid.setDetailsVisible(summary, false);
        mediaGrid.getDataProvider().refreshAll();
        uiContext.showSuccessNotification("Media updated successfully.");
        offerBarcodePrinting(updated.getTitle(), newCopies);
        return updated;
    }

    private static List<MediaInventoryItemDto> newlyAddedCopies(MediaCreateDto dto) {
        return dto.getInventoryItems().stream().filter(copy -> copy.id() == null).toList();
    }

    private void offerBarcodePrinting(String title, List<MediaInventoryItemDto> copies) {
        if (!copies.isEmpty()) {
            new BarcodePrintDialog(inventoryContext.getBarcodePrintService(), title, copies).open();
        }
    }

    private void confirmDelete(MediaSummaryDto medium) {
        ConfirmDialog dialog = getConfirmDialog(
                "Delete Medium?",
                new Html("<p>You are about to delete the medium:<br /><em>" +
                        medium.title() +
                        "</em><br />This action cannot be undone!</p>"),
                _ -> delete(medium)
        );
        dialog.open();
    }

    private void delete(MediaSummaryDto medium) {
        inventoryContext.getMediaManagementService().deleteMedium(medium.id());
        refreshGrid();
    }

    private void refreshGrid() {
        mediaGrid.getDataProvider().refreshAll();
        mediaGrid.scrollToIndex(0);
    }
}
