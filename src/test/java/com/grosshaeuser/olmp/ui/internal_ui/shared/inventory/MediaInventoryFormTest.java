package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodePrintServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.InventoryNumberServiceImpl;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class MediaInventoryFormTest {
    private UI ui;
    private MediaForm<MediaEditDto> form;
    private MediaEditDto source;
    private final LocationSummaryDto shelf = new LocationSummaryDto(10L, "A1", "Main shelf");

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        var book = new MediaTypeSummaryDto(1L, "BOOK", "Book");
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        form = new MediaForm<>(MediaEditDto.class, new MediaFormOptions(List.of(book), List.of(), List.of(), List.of(shelf)), new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class));
        source = new MediaEditDto();
        source.setId(1L);
        source.setMediaTypeId(book.id());
        source.setTitle("Title");
        source.getInventoryItems().add(new MediaInventoryItemDto(2L, "existing-number", "existing-barcode"));
        form.read(source);
        descendants(form).filter(TabSheet.class::isInstance).map(TabSheet.class::cast)
                .findFirst().orElseThrow().setSelectedIndex(3);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void fourthTabShowsExistingCopiesAndIdentifiersAreReadOnly() {
        TabSheet sheet = descendants(form).filter(TabSheet.class::isInstance).map(TabSheet.class::cast)
                .findFirst().orElseThrow();
        assertThat(sheet.getTabCount()).isEqualTo(4);
        assertThat(sheet.getTabAt(3).getLabel()).isEqualTo("Inventory items");
        TextField number = field("Inventory number");
        assertThat(number.getValue()).isEqualTo("existing-number");
        assertThat(number.isReadOnly()).isTrue();
        assertThat(descendants(form).filter(Image.class::isInstance)).isEmpty();
        assertThat(button("Print barcode").isEnabled()).isTrue();
        assertThat(descendants(form).filter(TextField.class::isInstance).map(TextField.class::cast))
                .noneMatch(field -> field.getLabel().equals("Barcode"));
    }

    @Test
    void printingUsesTheCurrentCatalogueTitle() {
        ui.getInternals().setSession(mock(VaadinSession.class, RETURNS_DEEP_STUBS));
        var printService = mock(BarcodePrintService.class);
        when(printService.createPrintDocument("Changed title", source.getInventoryItems())).thenReturn("<html></html>");
        var book = new MediaTypeSummaryDto(1L, "BOOK", "Book");
        form = new MediaForm<>(MediaEditDto.class,
                new MediaFormOptions(List.of(book), List.of(), List.of(), List.of(shelf)),
                new InventoryNumberServiceImpl(), new BarcodeServiceImpl(), printService, mock(MediaReferenceService.class));
        form.read(source);
        field("Title").setValue("Changed title");
        descendants(form).filter(TabSheet.class::isInstance).map(TabSheet.class::cast)
                .findFirst().orElseThrow().setSelectedIndex(3);

        button("Print barcode").click();

        verify(printService).createPrintDocument("Changed title", source.getInventoryItems());
        assertThat(source.getTitle()).isEqualTo("Title");
    }

    @Test
    void existingCopyWithoutBarcodeRemainsReadable() {
        source.setInventoryItems(List.of(new MediaInventoryItemDto(2L, "legacy-number", null)));
        form.read(source);
        assertThat(field("Inventory number").getValue()).isEqualTo("legacy-number");
        assertThat(descendants(form).filter(Image.class::isInstance)).isEmpty();
        assertThat(button("Print barcode").isEnabled()).isFalse();
        assertThat(submission().getInventoryItems()).containsExactlyElementsOf(source.getInventoryItems());
    }

    @Test
    void individualAndBatchCopiesCanBeAddedAndRemovedWithoutMutatingSource() {
        button("Add copy").click();
        count().setValue(1);
        button("Add copies").click();
        List<TextField> numbers = descendants(form).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals("Inventory number")).toList();
        String immediateNumber = numbers.getLast().getValue();
        assertThat(descendants(form).filter(Image.class::isInstance)).isEmpty();
        button("Add copy").click();
        count().setValue(3);
        button("Add copies").click();
        MediaEditDto submitted = submission();
        assertThat(submitted.getInventoryItems()).hasSize(5);
        assertThat(submitted.getInventoryItems().getFirst().id()).isEqualTo(2L);
        assertThat(submitted.getInventoryItems().subList(1, 5)).allSatisfy(copy -> {
            assertThat(copy.id()).isNull();
            assertThat(copy.barcode()).isEqualTo(copy.inventoryNumber());
        });
        assertThat(submitted.getInventoryItems().get(1).inventoryNumber()).isEqualTo(immediateNumber);
        assertThat(submitted.getInventoryItems()).extracting(MediaInventoryItemDto::inventoryNumber).doesNotHaveDuplicates();
        assertThat(submission().getInventoryItems()).containsExactlyElementsOf(submitted.getInventoryItems());
        button("Remove copy").click();
        assertThat(submission().getInventoryItems()).hasSize(4).allSatisfy(copy -> assertThat(copy.id()).isNull());
        assertThat(source.getInventoryItems()).hasSize(1);
        form.read(source);
        assertThat(submission().getInventoryItems()).containsExactlyElementsOf(source.getInventoryItems());
        assertThat(count().getValue()).isEqualTo(1);
    }

    @Test
    void invalidBatchCountsAddNoCopiesAndCanBeCorrected() {
        button("Add copy").click();
        for (Integer value : new Integer[]{null, 0, -1, 1001}) {
            count().setValue(value);
            button("Add copies").click();
            assertThat(count().isInvalid()).isTrue();
            assertThat(submission().getInventoryItems()).hasSize(1);
        }
        count().setValue(2);
        button("Add copies").click();
        assertThat(count().isInvalid()).isFalse();
        assertThat(submission().getInventoryItems()).hasSize(3);
    }

    @Test
    void createDialogSubmitsCopiesAndCancelDoesNotSubmit() {
        List<MediaCreateDto> saved = new ArrayList<>();
        var book = new MediaTypeSummaryDto(1L, "BOOK", "Book");
        MediaCreateDialog dialog = new MediaCreateDialog(
                new MediaFormOptions(List.of(book), List.of(), List.of()), new InventoryNumberServiceImpl(), new BarcodeServiceImpl(), mock(MediaReferenceService.class), saved::add,
                exception -> { throw exception; });
        dialog.open();
        descendants(dialog).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals("Title")).findFirst().orElseThrow().setValue("New title");
        @SuppressWarnings("unchecked")
        var type = (com.vaadin.flow.component.combobox.ComboBox<MediaTypeSummaryDto>) descendants(dialog)
                .filter(com.vaadin.flow.component.combobox.ComboBox.class::isInstance).findFirst().orElseThrow();
        type.setValue(book);
        descendants(dialog).filter(TabSheet.class::isInstance).map(TabSheet.class::cast)
                .findFirst().orElseThrow().setSelectedIndex(3);
        descendants(dialog).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals("Add copy")).findFirst().orElseThrow().click();
        descendants(dialog).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals("Add copies") || "Add copies".equals(button.getElement().getAttribute("aria-label"))).findFirst().orElseThrow().click();
        Component actions = dialog.getFooter().getElement().getChild(0).getComponent().orElseThrow();
        descendants(actions).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals("Save")).findFirst().orElseThrow().click();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getInventoryItems()).hasSize(1);
        assertThat(dialog.isOpened()).isFalse();
        dialog.open();
        descendants(actions).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals("Cancel")).findFirst().orElseThrow().click();
        assertThat(saved).hasSize(1);
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void batchCopiesShareEnteredDetailsAndHaveUniqueIdentifiers() {
        button("Add copy").click();
        combo("Condition", 0).setValue(InventoryItemCondition.NEW);
        combo("Location", 0).setValue(shelf);
        DatePicker date = descendants(form).filter(DatePicker.class::isInstance).map(DatePicker.class::cast).findFirst().orElseThrow();
        date.setValue(LocalDate.of(2026, 10, 22));
        BigDecimalField cost = descendants(form).filter(BigDecimalField.class::isInstance).map(BigDecimalField.class::cast).findFirst().orElseThrow();
        cost.setValue(new BigDecimal("12.34"));
        descendants(form).filter(TextArea.class::isInstance).map(TextArea.class::cast)
                .filter(field -> field.getLabel().equals("Notes")).findFirst().orElseThrow().setValue("New delivery");
        count().setValue(3);
        button("Add copies").click();

        List<MediaInventoryItemDto> copies = submission().getInventoryItems().subList(1, 4);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(copy.condition()).isEqualTo(InventoryItemCondition.NEW);
            assertThat(copy.locationId()).isEqualTo(shelf.id());
            assertThat(copy.acquiredAt()).isEqualTo(LocalDate.of(2026, 10, 22));
            assertThat(copy.acquisitionCost()).isEqualByComparingTo("12.34");
            assertThat(copy.notes()).isEqualTo("New delivery");
            assertThat(copy.barcode()).isEqualTo(copy.inventoryNumber());
        });
        assertThat(copies).extracting(MediaInventoryItemDto::inventoryNumber).doesNotHaveDuplicates();
        assertThat(copies).extracting(MediaInventoryItemDto::barcode).doesNotHaveDuplicates();
        Component batchForm = Stream.iterate((Component) cost, component -> component.getParent().orElseThrow())
                .filter(component -> component.getElement().getClassList().contains("inline-create-form"))
                .findFirst().orElseThrow();
        assertThat(batchForm.isVisible()).isFalse();
        button("Add copy").click();
        assertThat(cost.isEmpty()).isTrue();
        assertThat(date.isEmpty()).isTrue();
        assertThat(count().getValue()).isEqualTo(1);
    }

    @Test
    void existingCopyDetailsAreShownAndLocationConditionAndNotesCanBeEdited() {
        var copy = new MediaInventoryItemDto(2L, "existing-number", "existing-barcode", InventoryItemCondition.WORN,
                shelf.id(), LocalDate.of(2026, 10, 1), new BigDecimal("23.45"), LocalDate.of(2026, 10, 20), "Keep notes");
        source.setInventoryItems(List.of(copy));
        form.read(source);
        assertThat(combo("Condition", 1).getValue()).isEqualTo(InventoryItemCondition.WORN);
        assertThat(combo("Location", 1).getValue()).isEqualTo(shelf);
        DatePicker date = descendants(form).filter(DatePicker.class::isInstance).map(DatePicker.class::cast).toList().getLast();
        assertThat(date.getValue()).isEqualTo(copy.acquiredAt());
        assertThat(date.isReadOnly()).isTrue();
        BigDecimalField cost = descendants(form).filter(BigDecimalField.class::isInstance).map(BigDecimalField.class::cast).toList().getLast();
        assertThat(cost.getValue()).isEqualByComparingTo(copy.acquisitionCost());
        assertThat(cost.isReadOnly()).isTrue();
        TextArea notes = descendants(form).filter(TextArea.class::isInstance).map(TextArea.class::cast)
                .filter(field -> field.getLabel().equals("Notes")).toList().getLast();
        assertThat(notes.getValue()).isEqualTo(copy.notes());
        assertThat(notes.isReadOnly()).isFalse();
        notes.setValue("Updated notes");
        combo("Condition", 1).setValue(InventoryItemCondition.DAMAGED);
        combo("Location", 1).clear();
        assertThat(submission().getInventoryItems()).containsExactly(new MediaInventoryItemDto(copy.id(), copy.inventoryNumber(),
                copy.barcode(), InventoryItemCondition.DAMAGED, null, copy.acquiredAt(), copy.acquisitionCost(), copy.withdrawnAt(), "Updated notes"));
        assertThat(source.getInventoryItems()).containsExactly(copy);
        notes.clear();
        assertThat(submission().getInventoryItems().getFirst().notes()).isNull();
        combo("Condition", 1).clear();
        assertThat(form.write(new MediaEditDto())).isFalse();
    }

    @Test
    void invalidBatchDetailsAddNothingAndCancelDiscardsDraft() {
        button("Add copy").click();
        BigDecimalField cost = descendants(form).filter(BigDecimalField.class::isInstance).map(BigDecimalField.class::cast).findFirst().orElseThrow();
        for (String invalid : List.of("-1", "1.234", "100000000")) {
            cost.setValue(new BigDecimal(invalid));
            button("Add copies").click();
            assertThat(cost.isInvalid()).isTrue();
            assertThat(submission().getInventoryItems()).hasSize(1);
        }
        button("Cancel adding copies").click();
        assertThat(cost.isEmpty()).isTrue();
        button("Add copy").click();
        button("Add copies").click();
        assertThat(submission().getInventoryItems()).hasSize(2);
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Object> combo(String label, int index) {
        return (ComboBox<Object>) descendants(form).filter(ComboBox.class::isInstance).map(ComboBox.class::cast)
                .filter(field -> field.getLabel().equals(label)).toList().get(index);
    }

    private MediaEditDto submission() {
        MediaEditDto target = new MediaEditDto();
        target.setId(source.getId());
        assertThat(form.write(target)).isTrue();
        return target;
    }

    private Button button(String label) {
        return descendants(form).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals(label) || label.equals(button.getElement().getAttribute("aria-label"))).findFirst().orElseThrow();
    }

    private TextField field(String label) {
        return descendants(form).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals(label)).findFirst().orElseThrow();
    }

    private IntegerField count() {
        return descendants(form).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .filter(field -> field.getLabel().equals("Number of copies to add")).findFirst().orElseThrow();
    }

    private static Stream<Component> descendants(Component root) {
        Stream<Component> children = root instanceof TabSheet sheet
                ? java.util.stream.IntStream.range(0, sheet.getTabCount()).mapToObj(sheet::getTabAt).map(sheet::getComponent)
                : root.getChildren();
        return Stream.concat(Stream.of(root), children.flatMap(MediaInventoryFormTest::descendants));
    }
}
