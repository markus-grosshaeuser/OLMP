package com.grosshaeuser.olmp.ui.internal_ui.shared.reservations;

import com.grosshaeuser.olmp.reservations.ReservationStatus;
import com.grosshaeuser.olmp.reservations.dto.ReservationDto;
import com.grosshaeuser.olmp.reservations.dto.ReservationEditDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

class ReservationEditComponent extends VerticalLayout implements Structurable {
    private final ReservationContext reservationContext;
    private final UiContext uiContext;
    private final ReservationDto reservation;
    private final Runnable onSaved;
    private final ComboBox<ReservationStatus> status = new ComboBox<>("Status");
    private final TextArea notes = new TextArea("Notes");
    private final Button save = new Button("Save changes", _ -> confirmAndSave());

    ReservationEditComponent(ReservationContext reservationContext, UiContext uiContext, ReservationDto reservation,
                             Runnable onSaved, Runnable onClose) {
        this.reservationContext = reservationContext;
        this.uiContext = uiContext;
        this.reservation = reservation;
        this.onSaved = onSaved;
        setWidthFull();
        setPadding(false);
        addClassName("reservation-edit-component");
        boolean pending = reservation.status() == ReservationStatus.PENDING;
        status.setItemLabelGenerator(ReservationManagementView::statusLabel);
        if (pending) status.setItems(ReservationStatus.values());
        else status.setItems(reservation.status());
        status.setValue(reservation.status());
        status.setRequiredIndicatorVisible(true);
        status.setReadOnly(!pending);
        status.setHelperText("Closed reservations remain in the history. Fulfillment does not check out a copy; use Loan management for checkout.");
        status.setWidthFull();
        notes.setValue(reservation.notes() == null ? "" : reservation.notes());
        notes.setWidthFull();
        save.addThemeVariants(ButtonVariant.PRIMARY);
        save.setDisableOnClick(true);
        add(createComponentHeader("Edit reservation", "Update the reservation status and notes."),
                new Span("Member: " + reservation.member().firstName() + " " + reservation.member().lastName()),
                new Span("Title: " + reservation.media().title()),
                new Span("Reserved: " + ReservationManagementView.formatDate(reservation.reservationDate())),
                status, notes,
                createActionBar(new Button[]{save}, new Button[0], new Button[]{new Button("Close", _ -> onClose.run())}));
    }

    private void confirmAndSave() {
        if (status.getValue() != null && status.getValue() != reservation.status()) {
            ConfirmDialog confirmation = new ConfirmDialog();
            confirmation.setHeader("Close reservation?");
            confirmation.setText("Change this reservation to " + ReservationManagementView.statusLabel(status.getValue())
                    + "? Its status cannot be changed afterwards.");
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
            status.setInvalid(status.getValue() == null);
            if (status.isInvalid()) {
                throw new IllegalArgumentException("A reservation status is required.");
            }
            reservationContext.getReservationService().updateReservation(ReservationEditDto.builder()
                    .id(reservation.id()).status(status.getValue()).notes(notes.getValue())
                    .expectedUpdatedAt(reservation.updatedAt()).build());
            uiContext.showSuccessNotification("Reservation updated.");
            onSaved.run();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        } finally {
            save.setEnabled(true);
        }
    }
}
