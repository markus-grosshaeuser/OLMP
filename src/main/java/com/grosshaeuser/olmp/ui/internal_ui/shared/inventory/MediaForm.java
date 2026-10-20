package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorWriteDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierWriteDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.service.BarcodeService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

class MediaForm<T extends MediaCreateDto> extends Composite<VerticalLayout> implements Structurable, Printing {

    private final BeanValidationBinder<T> binder;
    private final MediaFormOptions options;
    private final BarcodeService barcodeService;
    private final BarcodePrintService barcodePrintService;
    private final ComboBox<MediaTypeSummaryDto> mediaType = new ComboBox<>("Media type");
    private final ComboBox<PublisherSummaryDto> publisher = new ComboBox<>("Publisher");
    private final TextField title = new TextField("Title");
    private final TextField subtitle = new TextField("Subtitle");
    private final TextField edition = new TextField("Edition");
    private final IntegerField publicationYear = new IntegerField("Publication year");
    private final TextField languageCode = new TextField("Language code");
    private final TextField coverImageFileName = new TextField("Cover image filename");
    private final TextArea description = new TextArea("Description");
    private final VerticalLayout authorRows = new VerticalLayout();
    private final VerticalLayout identifierRows = new VerticalLayout();
    private final VerticalLayout inventoryRows = new VerticalLayout();
    private final List<InventoryRow> inventoryItems = new ArrayList<>();
    private final IntegerField copyCount = new IntegerField("Number of copies");
    private final Span inventoryCount = new Span();
    private final List<AuthorRow> authors = new ArrayList<>();
    private final List<IdentifierRow> identifiers = new ArrayList<>();
    private final Span validationMessage = new Span();

    MediaForm(Class<T> dtoType, MediaFormOptions options, BarcodeService barcodeService, BarcodePrintService barcodePrintService) {
        this.options = Objects.requireNonNull(options, "options");
        this.barcodeService = Objects.requireNonNull(barcodeService, "barcodeService");
        binder = new BeanValidationBinder<>(dtoType);
        this.barcodePrintService = barcodePrintService;
        configureFields();
        configureBindings();

        FormLayout catalogue = responsiveForm();
        catalogue.add(title, subtitle, mediaType, publisher, edition, publicationYear, languageCode, coverImageFileName, description);
        catalogue.setColspan(title, 2);
        catalogue.setColspan(subtitle, 2);
        catalogue.setColspan(description, 2);
        authorRows.setPadding(false);
        identifierRows.setPadding(false);
        inventoryRows.setPadding(false);
        Button addAuthor = new Button("Add author", _ -> addAuthor(new MediaAuthorWriteDto()));
        Button addIdentifier = new Button("Add identifier", _ -> addIdentifier(new MediaIdentifierWriteDto()));
        Button addCopy = new Button("Add copy", _ -> addInventoryItem(MediaInventoryItemDto.newCopy()));
        copyCount.setMin(1);
        copyCount.setMax(1000);
        copyCount.setValue(1);
        copyCount.setStepButtonsVisible(true);
        copyCount.setHelperText("Add between 1 and 1,000 copies per batch.");
        Button addCopies = new Button("Add copies", _ -> addInventoryBatch());
        HorizontalLayout inventoryActions = new HorizontalLayout(addCopy, copyCount, addCopies);
        inventoryActions.setAlignItems(FlexComponent.Alignment.BASELINE);
        inventoryActions.setWrap(true);
        inventoryCount.getElement().setAttribute("aria-live", "polite");
        updateInventoryCount();
        validationMessage.getElement().setAttribute("role", "alert");
        validationMessage.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        validationMessage.setVisible(false);

        getContent().setPadding(false);
        getContent().setWidthFull();

        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        sheet.add("Catalogue", createSection("Catalogue", "Title, publication information and description.", catalogue));
        sheet.add("Authors and contributors", createSection("Authors and contributors", "Assign authors, roles and display order.", authorRows, addAuthor));
        sheet.add("Identifiers", createSection("Identifiers", "Add identifiers such as ISBN, ISSN or DOI.", identifierRows, addIdentifier));
        sheet.add("Inventory items", createSection("Inventory items",
                "Manage physical copies. Inventory numbers and barcodes are generated when you add copies. Changes take effect when you save the medium.",
                inventoryActions, inventoryCount, inventoryRows));

        getContent().add(sheet, validationMessage);
    }

    void read(T source) {
        Objects.requireNonNull(source, "source");
        binder.readBean(source);
        authors.clear();
        authorRows.removeAll();
        source.getAuthors().forEach(this::addAuthor);
        identifiers.clear();
        identifierRows.removeAll();
        source.getIdentifiers().forEach(this::addIdentifier);
        inventoryItems.clear();
        inventoryRows.removeAll();
        source.getInventoryItems().forEach(this::addInventoryItem);
        updateInventoryCount();
        copyCount.setValue(1);
        copyCount.setInvalid(false);
        validationMessage.setVisible(false);
    }

    boolean write(T target) {
        validationMessage.setVisible(false);
        List<MediaAuthorWriteDto> authorDtos = new ArrayList<>();
        List<MediaIdentifierWriteDto> identifierDtos = new ArrayList<>();
        boolean valid = true;
        for (AuthorRow row : authors) {
            MediaAuthorWriteDto dto = new MediaAuthorWriteDto();
            valid = row.binder.writeBeanIfValid(dto) && valid;
            authorDtos.add(dto);
        }
        for (IdentifierRow row : identifiers) {
            MediaIdentifierWriteDto dto = new MediaIdentifierWriteDto();
            dto.setId(row.id);
            valid = row.binder.writeBeanIfValid(dto) && valid;
            identifierDtos.add(dto);
        }
        valid = binder.writeBeanIfValid(target) && valid;
        if (!valid) {
            return false;
        }
        if (hasDuplicateAuthors(authorDtos)) {
            showValidationMessage("An author can only be assigned once for the same role.");
            return false;
        }
        if (hasDuplicateIdentifiers(identifierDtos)) {
            showValidationMessage("Each identifier type and value combination must be unique.");
            return false;
        }
        target.setAuthors(authorDtos);
        target.setIdentifiers(identifierDtos);
        target.setInventoryItems(new ArrayList<>(inventoryItems.stream().map(row -> row.dto).toList()));
        return true;
    }

    private void configureFields() {
        mediaType.setItems(options.mediaTypes());
        mediaType.setItemLabelGenerator(type -> type.name() + " (" + type.code() + ")");
        publisher.setItems(options.publishers());
        publisher.setItemLabelGenerator(PublisherSummaryDto::name);
        publisher.setClearButtonVisible(true);
        title.setMaxLength(500);
        subtitle.setMaxLength(500);
        edition.setMaxLength(100);
        languageCode.setMaxLength(10);
        languageCode.setHelperText("For example: en or de");
        coverImageFileName.setMaxLength(100);
        publicationYear.setMin(1);
        publicationYear.setMax(9999);
        description.setMinHeight("8rem");
        List.<HasSize>of(mediaType, publisher, title, subtitle, edition, publicationYear, languageCode, coverImageFileName, description).forEach(HasSize::setWidthFull);
    }

    private void configureBindings() {
        binder.forField(title).asRequired("Title is required").bind("title");
        binder.forField(subtitle).withNullRepresentation("").bind("subtitle");
        binder.forField(edition).withNullRepresentation("").bind("edition");
        binder.forField(languageCode).withNullRepresentation("").bind("languageCode");
        binder.forField(coverImageFileName).withNullRepresentation("").bind("coverImageFileName");
        binder.forField(description).withNullRepresentation("").bind("description");
        binder.forField(mediaType).asRequired("Media type is required")
                .withConverter(value -> value == null ? null : value.id(),
                        id -> lookup(options.mediaTypes(), MediaTypeSummaryDto::id, id, "Media type"))
                .bind("mediaTypeId");
        binder.forField(publisher)
                .withConverter(value -> value == null ? null : value.id(),
                        id -> lookup(options.publishers(), PublisherSummaryDto::id, id, "Publisher"))
                .bind("publisherId");
        binder.forField(publicationYear)
                .withValidator(value -> value == null || value >= 1 && value <= 9999,
                        "Publication year must be between 1 and 9999")
                .withConverter(value -> value == null ? null : value.shortValue(),
                        value -> value == null ? null : value.intValue())
                .bind("publicationYear");
    }

    private void addAuthor(MediaAuthorWriteDto dto) {
        AuthorRow row = new AuthorRow(dto);
        authors.add(row);
        authorRows.add(row.layout);
    }

    private void addIdentifier(MediaIdentifierWriteDto dto) {
        IdentifierRow row = new IdentifierRow(dto);
        identifiers.add(row);
        identifierRows.add(row.layout);
    }

    private void addInventoryItem(MediaInventoryItemDto dto) {
        InventoryRow row = new InventoryRow(dto);
        inventoryItems.add(row);
        inventoryRows.add(row.layout);
        updateInventoryCount();
    }

    private void addInventoryBatch() {
        Integer count = copyCount.getValue();
        if (count == null || count < 1 || count > 1000) {
            copyCount.setErrorMessage("Enter a number of copies between 1 and 1,000.");
            copyCount.setInvalid(true);
            return;
        }
        copyCount.setInvalid(false);
        for (int i = 0; i < count; i++) {
            addInventoryItem(MediaInventoryItemDto.newCopy());
        }
    }

    private void updateInventoryCount() {
        inventoryCount.setText("Copies: " + inventoryItems.size());
    }

    private void showValidationMessage(String message) {
        validationMessage.setText(message);
        validationMessage.setVisible(true);
    }

    private static boolean hasDuplicateAuthors(List<MediaAuthorWriteDto> rows) {
        record Key(Long authorId, String role) { }
        var seen = new HashSet<Key>();
        return rows.stream().anyMatch(row -> !seen.add(new Key(row.getAuthorId(), row.getRole())));
    }

    private static boolean hasDuplicateIdentifiers(List<MediaIdentifierWriteDto> rows) {
        record Key(String type, String value) { }
        var seen = new HashSet<Key>();
        return rows.stream().anyMatch(row -> !seen.add(new Key(row.getIdentifierType(), row.getIdentifierValue())));
    }

    private static FormLayout responsiveForm() {
        FormLayout layout = new FormLayout();
        layout.setWidthFull();
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40rem", 2)
        );
        return layout;
    }

    private static <R> R lookup(List<R> options, Function<R, Long> idGetter, Long id, String label) {
        if (id == null) {
            return null;
        }
        return options.stream().filter(option -> id.equals(idGetter.apply(option))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(label + " is no longer available. Reopen the form."));
    }

    static String authorName(AuthorSummaryDto author) {
        return author.firstName() == null || author.firstName().isBlank() ? author.lastName()
                : author.firstName() + " " + author.lastName();
    }

    private class InventoryRow {
        private final MediaInventoryItemDto dto;
        private final HorizontalLayout layout = new HorizontalLayout();

        InventoryRow(MediaInventoryItemDto dto) {
            this.dto = dto;
            TextField number = new TextField("Inventory number");
            number.setValue(Objects.requireNonNullElse(dto.inventoryNumber(), ""));
            number.setReadOnly(true);
            number.setWidthFull();
            VerticalLayout barcode = new VerticalLayout();
            barcode.setPadding(false);
            barcode.setSpacing(false);
            if (dto.barcode() == null || dto.barcode().isBlank()) {
                barcode.add(new Span("No barcode"));
            } else {
                String source = "data:image/png;base64," + Base64.getEncoder()
                        .encodeToString(barcodeService.generateBarcodePng(dto.barcode()));
                Image image = new Image(source, "Barcode " + dto.barcode());
                image.getStyle().set("max-width", "100%").set("height", "auto");
                barcode.add(image);
            }
            Button print = new Button(VaadinIcon.PRINT.create());
            print.setEnabled(false);
            if (barcodePrintService != null) {
                print.setEnabled(true);
                print.addClickListener(_ -> {
                    String document = barcodePrintService.createPrintDocument("", List.of(dto));
                    UI.getCurrentOrThrow().getPage().executeJs(jsPrintExpression, document);
                });
            }
            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                inventoryItems.remove(this);
                inventoryRows.remove(layout);
                updateInventoryCount();
            });
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            remove.setAriaLabel("Remove copy");
            layout.setWidthFull();
            layout.setWrap(false);
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.add(number, barcode, print, remove);
            layout.setFlexGrow(1, number, barcode);
        }
    }

    private class AuthorRow {
        private final BeanValidationBinder<MediaAuthorWriteDto> binder = new BeanValidationBinder<>(MediaAuthorWriteDto.class);
        private final HorizontalLayout layout = new HorizontalLayout();

        AuthorRow(MediaAuthorWriteDto dto) {
            ComboBox<AuthorSummaryDto> author = new ComboBox<>("Author");
            author.setItems(options.authors());
            author.setItemLabelGenerator(MediaForm::authorName);
            TextField role = new TextField("Role");
            role.setMaxLength(50);
            role.setHelperText("For example: AUTHOR, EDITOR or TRANSLATOR");
            IntegerField order = new IntegerField("Display order");
            order.setMin(0);
            order.setMax(Short.MAX_VALUE);
            order.setStepButtonsVisible(true);
            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                authors.remove(this);
                authorRows.remove(layout);
            });
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            remove.setAriaLabel("Remove author");
            binder.forField(author).asRequired("Choose an author")
                    .withConverter(value -> value == null ? null : value.id(),
                            id -> lookup(options.authors(), AuthorSummaryDto::id, id, "Author"))
                    .bind("authorId");
            binder.forField(role).asRequired("Role is required")
                    .withConverter(String::strip, value -> value == null ? "" : value).bind("role");
            binder.forField(order).asRequired("Display order is required")
                    .withValidator(value -> value >= 0 && value <= Short.MAX_VALUE,
                            "Display order must be between 0 and " + Short.MAX_VALUE)
                    .withConverter(Integer::shortValue, Short::intValue).bind("sortOrder");
            List.<HasSize>of(author, role).forEach(HasSize::setWidthFull);
            layout.setSizeFull();
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.add(author, role, order, remove);
            layout.setFlexGrow(1, author);
            layout.setFlexGrow(1, role);
            binder.readBean(dto);
        }
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
