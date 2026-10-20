package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.MediaInventoryItemDto;
import com.grosshaeuser.olmp.inventory.service.BarcodePrintService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Objects;

class BarcodePrintDialog extends Dialog implements Printing {
    private final BarcodePrintService printService;
    private final String mediaTitle;
    private final List<MediaInventoryItemDto> copies;
    private final Span error = new Span();
    private final Button print = new Button("Yes", _ -> print());

    BarcodePrintDialog(BarcodePrintService printService, String mediaTitle, List<MediaInventoryItemDto> copies) {
        this.printService = Objects.requireNonNull(printService, "printService");
        this.mediaTitle = Objects.requireNonNull(mediaTitle, "mediaTitle");
        this.copies = List.copyOf(copies);
        setHeaderTitle("Print barcode labels?");
        setWidth("30rem");
        setMaxWidth("95vw");
        setCloseOnOutsideClick(false);
        error.getElement().setAttribute("role", "alert");
        error.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        error.setVisible(false);
        String question = copies.size() == 1 ? "Print the barcode label for the newly added copy?"
                : "Print barcode labels for the " + copies.size() + " newly added copies?";
        add(new VerticalLayout(
                new Span("The medium was saved. " + question),
                new Span("Your browser's print dialog will open. Choose your printer and print on A4 paper."), error));
        print.addThemeVariants(ButtonVariant.PRIMARY);
        getFooter().add(new Button("No", _ -> close()), print);
    }

    private void print() {
        error.setVisible(false);
        print.setEnabled(false);
        try {
            String document = printService.createPrintDocument(mediaTitle, copies);
            UI.getCurrentOrThrow().getPage().executeJs(jsPrintExpression, document).then(Boolean.class, _ -> close(), _ -> printFailed());
        } catch (RuntimeException exception) {
            printFailed();
        }
    }

    private void printFailed() {
        error.setText("The medium is saved, but printing could not be started. Try Yes again or choose No.");
        error.setVisible(true);
        print.setEnabled(true);
    }
}
