package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.InventoryNumberServiceImpl;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BarcodePrintingTest {
    private static final MediaTypeSummaryDto BOOK = new MediaTypeSummaryDto(1L, "BOOK", "Book");
    private static final MediaSummaryDto SUMMARY = new MediaSummaryDto(1L, "Title", BOOK, null, null, List.of());
    private UI ui;
    private MediaManagementService media;
    private BarcodePrintService labels;
    private UiExceptionHandler errors;
    private MediaManagementView view;

    @BeforeEach
    void setUp() {
        ui = new UI();
        ui.getInternals().setSession(mock(VaadinSession.class, RETURNS_DEEP_STUBS));
        UI.setCurrent(ui);
        media = mock(MediaManagementService.class);
        labels = mock(BarcodePrintService.class);
        errors = mock(UiExceptionHandler.class);
        when(media.findMedia(anyString(), any())).thenReturn(List.of(SUMMARY));
        when(media.createMediaWithInventory(any())).thenReturn(SUMMARY);
        when(labels.createPrintDocument(anyString(), anyList())).thenReturn("<!doctype html><html>Labels</html>");
        view = new MediaManagementView(media,
                () -> new MediaFormOptions(List.of(BOOK), List.of(), List.of()), new InventoryNumberServiceImpl(), new BarcodeServiceImpl(),
                labels, mock(MediaReferenceService.class), errors, mock(UiNotificationHandler.class));
        ui.add(view);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void successfulCreateOffersPrintingAndNoSkipsRendering() {
        MediaCreateDialog dialog = createDialog();
        integerField(dialog, "Number of copies to add").setValue(1);
        button(dialog, "Add copies").click();
        button(dialog.getFooter(), "Save").click();
        assertThat(dialog.isOpened()).isFalse();
        BarcodePrintDialog prompt = prompt();
        assertThat(prompt.isOpened()).isTrue();
        button(prompt.getFooter(), "No").click();
        assertThat(prompt.isOpened()).isFalse();
        verify(media).createMediaWithInventory(any());
        verifyNoInteractions(labels);
    }

    @Test
    void successfulSaveWithoutNewCopiesDoesNotOfferPrinting() {
        button(createDialog().getFooter(), "Save").click();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        assertThat(descendants(ui).filter(BarcodePrintDialog.class::isInstance)).isEmpty();
        verifyNoInteractions(labels);
    }

    @Test
    void failedSaveDoesNotOfferPrintingAndCanBeRetried() {
        when(media.createMediaWithInventory(any())).thenThrow(new IllegalArgumentException("Save failed"));
        MediaCreateDialog dialog = createDialog();
        integerField(dialog, "Number of copies to add").setValue(1);
        button(dialog, "Add copies").click();
        button(dialog.getFooter(), "Save").click();
        assertThat(dialog.isOpened()).isTrue();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        assertThat(descendants(ui).filter(BarcodePrintDialog.class::isInstance)).isEmpty();
        verify(errors).handleException(any(IllegalArgumentException.class));
        verifyNoInteractions(labels);
    }

    @Test
    void editPrintsOnlyRemainingNewCopiesAndDoesNotOfferThemAgainOnNextSave() {
        MediaEditDto loaded = new MediaEditDto();
        loaded.setId(1L);
        loaded.setMediaTypeId(BOOK.id());
        loaded.setTitle("Title");
        loaded.setInventoryItems(List.of(new MediaInventoryItemDto(2L, "old-number", "old-barcode")));
        when(media.findMediaWithInventoryForEdit(1L)).thenReturn(Optional.of(loaded));
        when(media.updateMediaWithInventory(any())).thenAnswer(invocation -> {
            MediaEditDto submitted = invocation.getArgument(0);
            List<MediaInventoryItemDto> saved = new ArrayList<>();
            for (var copy : submitted.getInventoryItems()) {
                saved.add(new MediaInventoryItemDto(copy.id() == null ? 100L + saved.size() : copy.id(),
                        copy.inventoryNumber(), copy.barcode()));
            }
            submitted.setInventoryItems(saved);
            return submitted;
        });
        MediaEditComponent editor = ReflectionTestUtils.invokeMethod(view, "createEditor", SUMMARY);
        ui.add(editor);
        integerField(editor, "Number of copies to add").setValue(1);
        button(editor, "Add copies").click();
        button(editor, "Remove copy").click();
        integerField(editor, "Number of copies to add").setValue(1);
        button(editor, "Add copies").click();
        button(editor, "Remove copy").click();
        String kept = descendants(editor).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals("Inventory number")).findFirst().orElseThrow().getValue();
        button(editor, "Save changes").click();
        BarcodePrintDialog prompt = prompt();
        button(prompt.getFooter(), "Yes").click();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MediaInventoryItemDto>> copies = ArgumentCaptor.forClass(List.class);
        verify(labels).createPrintDocument(eq("Title"), copies.capture());
        assertThat(copies.getValue()).hasSize(1);
        assertThat(copies.getValue().getFirst().barcode()).isEqualTo(kept);
        printInvocation().complete(JsonNodeFactory.instance.booleanNode(true));
        assertThat(prompt.isOpened()).isFalse();
        button(editor, "Save changes").click();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        assertThat(descendants(ui).filter(BarcodePrintDialog.class::isInstance)
                .map(BarcodePrintDialog.class::cast).filter(Dialog::isOpened)).isEmpty();
        verify(media, times(2)).updateMediaWithInventory(any());
        verifyNoMoreInteractions(labels);
    }

    @Test
    void printFailureCanBeRetriedWithoutSavingAgain() {
        when(labels.createPrintDocument(anyString(), anyList()))
                .thenThrow(new IllegalArgumentException("Printer document failed"))
                .thenReturn("<!doctype html><html>Labels</html>");
        MediaCreateDialog dialog = createDialog();
        integerField(dialog, "Number of copies to add").setValue(1);
        button(dialog, "Add copies").click();
        button(dialog.getFooter(), "Save").click();
        BarcodePrintDialog prompt = prompt();
        button(prompt.getFooter(), "Yes").click();
        assertThat(prompt.isOpened()).isTrue();
        assertThat(button(prompt.getFooter(), "Yes").isEnabled()).isTrue();
        assertThat(descendants(prompt).filter(Span.class::isInstance).map(Span.class::cast))
                .anyMatch(span -> span.isVisible() && span.getText().contains("medium is saved"));
        button(prompt.getFooter(), "Yes").click();
        PendingJavaScriptInvocation invocation = printInvocation();
        assertThat(invocation.getInvocation().getParameters()).contains("<!doctype html><html>Labels</html>");
        invocation.completeExceptionally(JsonNodeFactory.instance.stringNode("Browser could not print"));
        assertThat(prompt.isOpened()).isTrue();
        assertThat(button(prompt.getFooter(), "Yes").isEnabled()).isTrue();
        button(prompt.getFooter(), "No").click();
        verify(media, times(1)).createMediaWithInventory(any());
    }

    private MediaCreateDialog createDialog() {
        button(view, "Create media").click();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        MediaCreateDialog dialog = descendants(ui).filter(MediaCreateDialog.class::isInstance)
                .map(MediaCreateDialog.class::cast).findFirst().orElseThrow();
        descendants(dialog).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals("Title")).findFirst().orElseThrow().setValue("Title");
        @SuppressWarnings("unchecked")
        ComboBox<MediaTypeSummaryDto> type = (ComboBox<MediaTypeSummaryDto>) descendants(dialog)
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        type.setValue(BOOK);
        return dialog;
    }

    private BarcodePrintDialog prompt() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return descendants(ui).filter(BarcodePrintDialog.class::isInstance).map(BarcodePrintDialog.class::cast)
                .filter(Dialog::isOpened).findFirst().orElseThrow();
    }

    private PendingJavaScriptInvocation printInvocation() {
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .filter(invocation -> invocation.getInvocation().getExpression().contains("contentWindow.print()"))
                .findFirst().orElseThrow();
    }

    private static Button button(Component root, String label) {
        Button result = descendants(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals(label) || label.equals(button.getElement().getAttribute("aria-label"))).findFirst().orElseThrow();
        descendants(root).filter(TabSheet.class::isInstance).map(TabSheet.class::cast).forEach(sheet -> {
            for (int index = 0; index < sheet.getTabCount(); index++) {
                if (descendants(sheet.getComponent(sheet.getTabAt(index))).anyMatch(component -> component == result)) {
                    sheet.setSelectedIndex(index);
                    break;
                }
            }
        });
        return result;
    }

    private static IntegerField integerField(Component root, String label) {
        IntegerField result = descendants(root).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .filter(field -> field.getLabel().equals(label) || label.equals(field.getElement().getAttribute("aria-label"))).findFirst().orElseThrow();
        descendants(root).filter(TabSheet.class::isInstance).map(TabSheet.class::cast).forEach(sheet -> {
            for (int index = 0; index < sheet.getTabCount(); index++) {
                if (descendants(sheet.getComponent(sheet.getTabAt(index))).anyMatch(component -> component == result)) {
                    sheet.setSelectedIndex(index);
                    break;
                }
            }
        });
        return result;
    }

    private static Button button(Dialog.DialogFooter footer, String label) {
        return footer.getElement().getChildren().flatMap(element -> element.getComponent().stream())
                .flatMap(BarcodePrintingTest::descendants).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals(label)).findFirst().orElseThrow();
    }

    private static Stream<Component> descendants(Component root) {
        Stream<Component> children = root instanceof TabSheet sheet
                ? java.util.stream.IntStream.range(0, sheet.getTabCount()).mapToObj(sheet::getTabAt).map(sheet::getComponent)
                : root.getChildren();
        return Stream.concat(Stream.of(root), children.flatMap(BarcodePrintingTest::descendants));
    }
}
