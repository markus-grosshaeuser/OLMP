package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCreateDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.InventoryNumberService;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import static com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormSupport.lookup;
import static com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormSupport.responsiveForm;

class MediaInventoryForm extends Composite<Div> implements Structurable, Printing {

    private final List<LocationSummaryDto> locations;
    private final InventoryNumberService inventoryNumberService;
    private final BarcodePrintService barcodePrintService;
    private final Supplier<String> mediaTitle;
    private final VerticalLayout inventoryRows = new VerticalLayout();
    private final List<InventoryRow> inventoryItems = new ArrayList<>();
    private final IntegerField copyCount = new IntegerField("Number of copies");
    private final VerticalLayout copyForm = new VerticalLayout();
    private final InventoryFields batchFields;
    private final Span inventoryCount = new Span();

    MediaInventoryForm(MediaFormOptions options, InventoryContext inventoryContext, Supplier<String> mediaTitle) {
        this.locations = List.copyOf(options.locations());
        this.inventoryNumberService = inventoryContext.getInventoryNumberService();
        this.barcodePrintService = inventoryContext.getBarcodePrintService();
        this.mediaTitle = Objects.requireNonNull(mediaTitle, "mediaTitle");
        inventoryRows.setPadding(false);
        batchFields = new InventoryFields();
        copyCount.setMin(1);
        copyCount.setMax(1000);
        copyCount.setValue(1);
        copyCount.setStepButtonsVisible(true);
        copyCount.setHelperText("Add between 1 and 1,000 copies per batch.");
        copyCount.setAriaLabel("Number of copies to add");
        copyCount.setLabel("Number of copies to add");
        Button addCopies = new Button("Add copies", _ -> addInventoryBatch());
        Button cancelCopies = new Button("Cancel", _ -> resetCopyForm());
        cancelCopies.setAriaLabel("Cancel adding copies");
        HorizontalLayout inventoryActions = new HorizontalLayout(addCopies, cancelCopies);
        inventoryActions.setAlignItems(FlexComponent.Alignment.BASELINE);
        inventoryActions.setWrap(true);
        batchFields.layout.add(copyCount);
        copyForm.addClassName("inline-create-form");
        copyForm.setWidthFull();
        copyForm.add(batchFields.layout, inventoryActions);
        resetCopyForm();
        Button addCopy = new Button("Add copy", _ -> {
            copyForm.setVisible(true);
            batchFields.condition.focus();
        });
        inventoryCount.getElement().setAttribute("aria-live", "polite");
        updateInventoryCount();
        getContent().add(createSection("Inventory items",
                "Manage physical copies. Inventory numbers and barcodes are generated when you add copies. Changes take effect when you save the medium.",
                addCopy, copyForm, inventoryCount, inventoryRows));
        getContent().setWidthFull();
    }

    void read(List<MediaInventoryItemDto> source) {
        inventoryItems.clear();
        inventoryRows.removeAll();
        source.forEach(this::addInventoryItem);
        updateInventoryCount();
        resetCopyForm();
    }

    boolean write(List<MediaInventoryItemDto> target) {
        List<MediaInventoryItemDto> values = new ArrayList<>();
        boolean valid = true;
        for (InventoryRow row : inventoryItems) {
            InventoryItemCreateDto dto = new InventoryItemCreateDto();
            valid = row.fields.binder.writeBeanIfValid(dto) && valid;
            values.add(row.fields.copy(row.dto, dto));
        }
        if (valid) {
            target.addAll(values);
        }
        return valid;
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
        InventoryItemCreateDto values = new InventoryItemCreateDto();
        if (!batchFields.binder.writeBeanIfValid(values)) {
            return;
        }
        for (int i = 0; i < count; i++) {
            String inventoryNumber = inventoryNumberService.generate();
            addInventoryItem(batchFields.copy(new MediaInventoryItemDto(null, inventoryNumber, inventoryNumber), values));
        }
        resetCopyForm();
    }

    private void resetCopyForm() {
        batchFields.binder.readBean(new InventoryItemCreateDto());
        copyCount.setValue(1);
        copyCount.setInvalid(false);
        copyForm.setVisible(false);
    }

    private void updateInventoryCount() {
        inventoryCount.setText("Copies: " + inventoryItems.size());
    }

    private class InventoryFields {
        private final BeanValidationBinder<InventoryItemCreateDto> binder = new BeanValidationBinder<>(InventoryItemCreateDto.class);
        private final FormLayout layout = responsiveForm();
        private final ComboBox<InventoryItemCondition> condition = new ComboBox<>("Condition");
        private final DatePicker acquiredAt = new DatePicker("Acquisition date");
        private final BigDecimalField acquisitionCost = new BigDecimalField("Acquisition cost");

        InventoryFields() {
            condition.setItems(InventoryItemCondition.values());
            condition.setItemLabelGenerator(value -> value.name().charAt(0) + value.name().substring(1).toLowerCase(Locale.ROOT));
            ComboBox<LocationSummaryDto> location = new ComboBox<>("Location");
            location.setItems(locations);
            location.setItemLabelGenerator(value -> value.name() + " (" + value.code() + ")");
            location.setClearButtonVisible(true);
            binder.forField(condition).asRequired("Condition is required").bind("condition");
            binder.forField(location)
                    .withConverter(value -> value == null ? null : value.id(),
                            id -> lookup(locations, LocationSummaryDto::id, id, "Location"))
                    .bind("locationId");
            binder.forField(acquiredAt).bind("acquiredAt");
            binder.forField(acquisitionCost).bind("acquisitionCost");
            TextArea notes = new TextArea("Notes");
            binder.forField(notes).withNullRepresentation("").bind("notes");
            List.<HasSize>of(condition, location, acquiredAt, acquisitionCost, notes).forEach(HasSize::setWidthFull);
            notes.setWidthFull();
            HorizontalLayout detailsFields = new HorizontalLayout(condition, location, acquiredAt, acquisitionCost);
            detailsFields.setWidthFull();
            detailsFields.setWrap(false);
            detailsFields.setAlignItems(FlexComponent.Alignment.END);
            detailsFields.setFlexGrow(1, condition, location, acquiredAt, acquisitionCost);
            layout.addFormRow(detailsFields);
            layout.addFormRow(notes);
        }

        void read(MediaInventoryItemDto source) {
            InventoryItemCreateDto values = new InventoryItemCreateDto();
            values.setCondition(source.condition());
            values.setLocationId(source.locationId());
            values.setAcquiredAt(source.acquiredAt());
            values.setAcquisitionCost(source.acquisitionCost());
            values.setNotes(source.notes());
            binder.readBean(values);
        }

        MediaInventoryItemDto copy(MediaInventoryItemDto source, InventoryItemCreateDto values) {
            return new MediaInventoryItemDto(source.id(), source.inventoryNumber(), source.barcode(),
                    values.getCondition(), values.getLocationId(), values.getAcquiredAt(),
                    values.getAcquisitionCost(), source.withdrawnAt(), values.getNotes());
        }
    }

    private class InventoryRow {
        private final MediaInventoryItemDto dto;
        private final VerticalLayout layout = new VerticalLayout();
        private final InventoryFields fields = new InventoryFields();

        InventoryRow(MediaInventoryItemDto dto) {
            this.dto = dto;
            TextField number = new TextField("Inventory number");
            number.setValue(Objects.requireNonNullElse(dto.inventoryNumber(), ""));
            number.setReadOnly(true);
            number.setWidthFull();
            fields.read(dto);
            fields.acquiredAt.setReadOnly(true);
            fields.acquisitionCost.setReadOnly(true);
            Button print = new Button(VaadinIcon.PRINT.create());
            print.setAriaLabel("Print barcode");
            print.setTooltipText("Print barcode");
            print.setEnabled(dto.barcode() != null && !dto.barcode().isBlank());
            print.setVisible(false);
            if (barcodePrintService != null) {
                print.setVisible(true);
                print.addClickListener(_ -> {
                    String document = barcodePrintService.createPrintDocument(mediaTitle.get(), List.of(dto));
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
            layout.setPadding(false);
            layout.addClassName("inventory-row");
            HorizontalLayout header = new HorizontalLayout(number, print, remove);
            header.setWidthFull();
            header.setWrap(false);
            header.setAlignItems(FlexComponent.Alignment.END);
            header.setFlexGrow(1, number);
            layout.add(header, fields.layout);
        }
    }
}
