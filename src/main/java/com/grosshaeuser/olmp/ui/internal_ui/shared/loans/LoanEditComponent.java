package com.grosshaeuser.olmp.ui.internal_ui.shared.loans;

import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.LoanDto;
import com.grosshaeuser.olmp.loans.dto.LoanEditDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

class LoanEditComponent extends VerticalLayout implements Structurable {
    private final LoanContext loanContext;
    private final UiContext uiContext;
    private final LoanDto loan;
    private final Runnable onSaved;
    private final DateTimePicker due = new DateTimePicker("Due date");
    private final LocalDateTime initialDueValue;
    private final ComboBox<LoanStatus> status = new ComboBox<>("Status");
    private final TextArea notes = new TextArea("Notes");
    private final Button save = new Button("Save changes", _ -> confirmAndSave());

    LoanEditComponent(LoanContext loanContext, UiContext uiContext, LoanDto loan, Runnable onSaved, Runnable onClose) {
        this.loanContext = loanContext;
        this.loan = loan;
        this.onSaved = onSaved;
        this.uiContext = uiContext;
        setWidthFull();
        setPadding(false);
        addClassName("loan-edit-component");

        boolean open = loan.status() == LoanStatus.ACTIVE || loan.status() == LoanStatus.OVERDUE;
        due.setValue(loan.dueDate().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        initialDueValue = due.getValue();
        due.setRequiredIndicatorVisible(true);
        due.setReadOnly(!open);
        status.setItemLabelGenerator(LoanManagementView::statusLabel);
        status.setItems(open ? List.of(loan.status(), LoanStatus.RETURNED, LoanStatus.LOST, LoanStatus.CANCELLED)
                : loan.status() == LoanStatus.LOST ? List.of(LoanStatus.LOST, LoanStatus.RETURNED) : List.of(loan.status()));
        status.setValue(loan.status());
        status.setReadOnly(!open && loan.status() != LoanStatus.LOST);
        status.setHelperText(loan.status() == LoanStatus.LOST
                ? "A recovered copy is marked unusable until its condition is checked in inventory."
                : "Overdue is calculated from the due date. Closed loans remain in the history.");
        notes.setValue(loan.notes() == null ? "" : loan.notes());
        HorizontalLayout dueAndStatus = new HorizontalLayout(due, status);
        VerticalLayout content = new VerticalLayout(
                new Span("Member: " + loan.member().firstName() + " " + loan.member().lastName()),
                new Span(
                        new Span("Title: " + loan.inventoryItem().media().title()),
                        new Span(" (" + loan.inventoryItem().inventoryNumber() + ")")
                ),
                new Span(
                        new Span("Checked out: " + LoanManagementView.formatDate(loan.loanDate())),
                        new Span(", Returned: " + LoanManagementView.formatDate(loan.returnDate()))
                ),
                new Span("Issued by: " + (loan.employee() == null ? "—" : loan.employee().firstName() + " " + loan.employee().lastName())),
                dueAndStatus,
                notes);
        content.setPadding(false);
        dueAndStatus.setWidthFull();
        due.setWidthFull();
        status.setWidthFull();
        notes.setWidthFull();
        save.addThemeVariants(ButtonVariant.PRIMARY);
        save.setDisableOnClick(true);
        Button close = new Button("Close", _ -> onClose.run());
        add(createComponentHeader("Edit loan", "Update the due date, loan status and notes."),
                content, createActionBar(new Button[]{save}, new Button[0], new Button[]{close}));
    }

    private void confirmAndSave() {
        if (status.getValue() != loan.status() && (status.getValue() == LoanStatus.CANCELLED || status.getValue() == LoanStatus.LOST)) {
            ConfirmDialog confirmation = new ConfirmDialog();
            confirmation.setHeader(status.getValue() == LoanStatus.LOST ? "Mark copy as lost?" : "Cancel loan?");
            confirmation.setText(status.getValue() == LoanStatus.LOST
                    ? "The copy will be marked lost and unavailable for checkout."
                    : "Cancel only an erroneous checkout. For a physical return, choose Returned.");
            confirmation.setCancelable(true);
            confirmation.setConfirmText("Confirm");
            confirmation.addConfirmListener(_ -> save());
            confirmation.addOpenedChangeListener(event -> {
                if (!event.isOpened()) save.setEnabled(true);
            });
            confirmation.open();
        } else {
            save();
        }
    }

    private void save() {
        try {
            if (due.isInvalid() || due.getValue() == null || status.getValue() == null) {
                throw new IllegalArgumentException("A valid due date and status are required.");
            }
            var dueDate = due.getValue().equals(initialDueValue) ? loan.dueDate()
                    : due.getValue().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            loanContext.getLoanService().updateLoan(LoanEditDto.builder().id(loan.id()).dueDate(dueDate)
                    .status(status.getValue()).notes(notes.getValue()).expectedUpdatedAt(loan.updatedAt()).build());
            uiContext.showSuccessNotification("Loan updated.");
            onSaved.run();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        } finally {
            save.setEnabled(true);
        }
    }
}
