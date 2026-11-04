package com.grosshaeuser.olmp.ui.internal_ui.shared.loans;

import com.grosshaeuser.olmp.loans.dto.LoanBatchCreateDto;
import com.grosshaeuser.olmp.loans.dto.LoanBatchReturnDto;
import com.grosshaeuser.olmp.loans.dto.LoanCopyDto;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

class LoanBatchDialog extends Dialog {
    private final LoanContext context;
    private final boolean checkout;
    private final Runnable onSaved;
    private final List<LoanCopyDto> copies = new ArrayList<>();
    private final TextField membership = new TextField("Membership number");
    private final Span overdueWarning = new Span("Warning: this member has overdue loans.");
    private final DateTimePicker due = new DateTimePicker("Due date");
    private final TextArea notes = new TextArea("Notes");
    private final TextField code = new TextField("Inventory number or barcode");
    private final Grid<LoanCopyDto> basket = new Grid<>(LoanCopyDto.class, false);
    private final Button submit;

    LoanBatchDialog(LoanContext context, boolean checkout, Runnable onSaved) {
        this.context = context;
        this.checkout = checkout;
        this.onSaved = onSaved;
        setHeaderTitle(checkout ? "Check out copies" : "Return copies");
        setWidth("54em");
        setMaxWidth("95vw");
        setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        if (checkout) {
            membership.setWidthFull();
            membership.setRequiredIndicatorVisible(true);
            membership.setErrorMessage("A membership number is required.");
            overdueWarning.setVisible(false);
            overdueWarning.getStyle()
                    .set("display", "block")
                    .set("color", "var(--lumo-warning-text-color, #8a5700)")
                    .set("font-size", "large");
            overdueWarning.getElement().setAttribute("role", "status");
            overdueWarning.getElement().setAttribute("aria-live", "polite");
            membership.setHelperComponent(new Div(
                    new Span("Scan the member's library card or enter the membership number."), overdueWarning));
            membership.addValueChangeListener(_ -> checkOverdueLoans());
            membership.setMaxLength(50);
            membership.addKeyPressListener(Key.ENTER, _ -> code.focus());
            due.setWidthFull();
            due.setRequiredIndicatorVisible(true);
            due.setErrorMessage("Enter a valid future due date.");
            due.setValue(LocalDate.now().plusDays(28).atTime(LocalTime.of(23, 59)));
            due.setHelperText("Defaults to 28 days; adjust for this checkout.");
            notes.setWidthFull();
            content.add(membership, due, notes);
        }
        code.setPlaceholder("Scan or enter a copy code, then press Enter");
        code.setClearButtonVisible(true);
        code.setMaxLength(100);
        code.addKeyPressListener(Key.ENTER, _ -> addCopy());
        Button add = new Button("Add copy", VaadinIcon.PLUS.create(), _ -> addCopy());
        HorizontalLayout scanner = new HorizontalLayout(code, add);
        scanner.setWidthFull();
        scanner.setAlignItems(VerticalLayout.Alignment.BASELINE);
        scanner.setFlexGrow(1, code);
        scanner.setWrap(true);

        basket.addColumn(LoanCopyDto::mediaTitle).setHeader("Title").setFlexGrow(2);
        basket.addColumn(LoanCopyDto::inventoryNumber).setHeader("Inventory number");
        basket.addComponentColumn(copy -> {
            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                copies.remove(copy);
                refreshBasket();
            });
            remove.setAriaLabel("Remove " + copy.inventoryNumber() + " from batch");
            return remove;
        }).setHeader("Remove").setFlexGrow(0).setAutoWidth(true);
        basket.setEmptyStateText("No copies added");
        basket.setHeight("16em");
        content.add(scanner, basket, new Span("The entire batch is processed together. Correct any errors before submitting again."));
        add(content);

        submit = new Button(checkout ? "Check out copies" : "Return copies", _ -> submit());
        submit.addThemeVariants(ButtonVariant.PRIMARY);
        submit.setDisableOnClick(true);
        getFooter().add(new Button("Cancel", _ -> close()), submit);
        refreshBasket();
        addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                if (checkout) membership.focus();
                else code.focus();
            }
        });
    }

    private void checkOverdueLoans() {
        overdueWarning.setVisible(false);
        if (membership.getValue().isBlank()) {
            return;
        }
        try {
            overdueWarning.setText("Warning: this member has overdue loans.");
            overdueWarning.setVisible(context.getLoanService().hasOverdueLoans(membership.getValue().strip()));
        } catch (RuntimeException exception) {
            overdueWarning.setText("Overdue loans could not be checked.");
            overdueWarning.setVisible(true);
            context.getUiContext().handleException(exception);
        }
    }

    private void addCopy() {
        try {
            LoanCopyDto copy = context.getLoanService().findCopy(code.getValue())
                    .orElseThrow(() -> new IllegalArgumentException("No copy found for this code."));
            if (copies.stream().anyMatch(existing -> existing.id().equals(copy.id()))) {
                context.getUiContext().showWarning("This copy is already in the batch.");
            } else {
                copies.add(copy);
                refreshBasket();
            }
            code.clear();
            code.focus();
        } catch (RuntimeException exception) {
            context.getUiContext().handleException(exception);
        }
    }

    private void refreshBasket() {
        basket.setItems(List.copyOf(copies));
        submit.setEnabled(!copies.isEmpty());
        submit.setText((checkout ? "Check out copies" : "Return copies") + " (" + copies.size() + ")");
    }

    private void submit() {
        try {
            if (!code.isEmpty()) {
                throw new IllegalArgumentException("Add the entered copy code to the batch before submitting.");
            }
            List<Long> ids = copies.stream().map(LoanCopyDto::id).toList();
            if (checkout) {
                membership.setInvalid(membership.getValue().isBlank());
                due.setInvalid(due.isInvalid() || due.getValue() == null || !due.getValue().isAfter(LocalDateTime.now()));
                if (membership.isInvalid() || due.isInvalid()) {
                    throw new IllegalArgumentException("Enter a membership number and a valid future due date.");
                }
                context.getLoanService().createLoans(LoanBatchCreateDto.builder()
                        .membershipNumber(membership.getValue().strip()).inventoryItemIds(ids)
                        .dueDate(due.getValue().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                        .notes(notes.getValue()).build());
            } else {
                context.getLoanService().returnLoans(LoanBatchReturnDto.builder().inventoryItemIds(ids).build());
            }
            close();
            context.getUiContext().showSuccessNotification(ids.size() + (checkout ? " copies checked out." : " copies returned."));
            onSaved.run();
        } catch (RuntimeException exception) {
            context.getUiContext().handleException(exception);
        } finally {
            submit.setEnabled(!copies.isEmpty());
        }
    }
}
