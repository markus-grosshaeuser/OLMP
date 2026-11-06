package com.grosshaeuser.olmp.ui.internal_ui.shared.reservations;

import com.grosshaeuser.olmp.reservations.dto.ReservationBatchCreateDto;
import com.grosshaeuser.olmp.reservations.dto.ReservationMediaDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

class ReservationBatchDialog extends Dialog {
    private final ReservationContext reservationContext;
    private final UiContext uiContext;
    private final Runnable onSaved;
    private final List<ReservationMediaDto> titles = new ArrayList<>();
    private final TextField membership = new TextField("Membership number");
    private final ComboBox<ReservationMediaDto> media = new ComboBox<>("Title");
    private final TextArea notes = new TextArea("Notes");
    private final Grid<ReservationMediaDto> basket = new Grid<>(ReservationMediaDto.class, false);
    private final Button submit = new Button("Create reservations", _ -> submit());

    ReservationBatchDialog(ReservationContext reservationContext, UiContext uiContext, Runnable onSaved) {
        this.reservationContext = reservationContext;
        this.uiContext = uiContext;
        this.onSaved = onSaved;
        setHeaderTitle("New reservations");
        setWidth("54em");
        setMaxWidth("95vw");
        setCloseOnOutsideClick(false);
        membership.setWidthFull();
        membership.setMaxLength(50);
        membership.setRequiredIndicatorVisible(true);
        membership.setErrorMessage("A membership number is required.");
        membership.setHelperText("Scan the member's library card or enter the membership number.");
        notes.setWidthFull();

        media.setPlaceholder("Search by title or identifier (e.g. ISBN)");
        media.setClearButtonVisible(true);
        media.setItemLabelGenerator(ReservationBatchDialog::mediaLabel);
        media.setPageSize(25);
        media.setItems(query -> {
            try {
                return reservationContext.getReservationService().findMedia(query.getFilter().orElse(""),
                        PageRequest.of(query.getPage(), query.getPageSize())).stream();
            } catch (RuntimeException exception) {
                uiContext.handleException(exception);
                return java.util.stream.Stream.empty();
            }
        });
        Button add = new Button("Add title", VaadinIcon.PLUS.create(), _ -> addTitle());
        HorizontalLayout selector = new HorizontalLayout(media, add);
        selector.setWidthFull();
        selector.setWrap(true);
        selector.setAlignItems(VerticalLayout.Alignment.BASELINE);
        selector.setFlexGrow(1, media);

        basket.addColumn(ReservationMediaDto::title).setHeader("Title").setFlexGrow(2);
        basket.addColumn(ReservationMediaDto::mediaType).setHeader("Type");
        basket.addColumn(ReservationMediaDto::publicationYear).setHeader("Year");
        basket.addComponentColumn(title -> {
            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                titles.remove(title);
                refreshBasket();
            });
            remove.setAriaLabel("Remove " + title.title() + " from batch");
            return remove;
        }).setHeader("Remove").setAutoWidth(true).setFlexGrow(0);
        basket.setEmptyStateText("No titles added");
        basket.setHeight("16em");
        VerticalLayout content = new VerticalLayout(membership, selector, basket, notes,
                new Span("Reservations apply to titles. The entire batch is created together for this member."));
        content.setPadding(false);
        add(content);
        submit.addThemeVariants(ButtonVariant.PRIMARY);
        submit.setDisableOnClick(true);
        getFooter().add(new Button("Cancel", _ -> close()), submit);
        refreshBasket();
        addOpenedChangeListener(event -> {
            if (event.isOpened()) membership.focus();
        });
    }

    private void addTitle() {
        ReservationMediaDto selected = media.getValue();
        if (selected == null) {
            uiContext.showWarning("Select a title to add.");
            return;
        }
        if (titles.stream().anyMatch(title -> title.id().equals(selected.id()))) {
            uiContext.showWarning("This title is already in the batch.");
        } else {
            titles.add(selected);
            refreshBasket();
        }
        media.clear();
        media.focus();
    }

    private void refreshBasket() {
        basket.setItems(List.copyOf(titles));
        submit.setEnabled(!titles.isEmpty());
        submit.setText("Create reservations (" + titles.size() + ")");
    }

    private void submit() {
        try {
            membership.setInvalid(membership.getValue().isBlank());
            if (membership.isInvalid()) {
                throw new IllegalArgumentException("A membership number is required.");
            }
            if (media.getValue() != null) {
                throw new IllegalArgumentException("Add the selected title to the batch before submitting.");
            }
            if (titles.isEmpty()) {
                throw new IllegalArgumentException("Add at least one title.");
            }
            reservationContext.getReservationService().createReservations(ReservationBatchCreateDto.builder()
                    .membershipNumber(membership.getValue().strip())
                    .mediaIds(titles.stream().map(ReservationMediaDto::id).toList()).notes(notes.getValue()).build());
            close();
            uiContext.showSuccessNotification(titles.size() + " reservations created.");
            onSaved.run();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        } finally {
            submit.setEnabled(!titles.isEmpty());
        }
    }

    private static String mediaLabel(ReservationMediaDto title) {
        return title.title() + " (" + title.mediaType()
                + (title.publicationYear() == null ? "" : ", " + title.publicationYear()) + ")";
    }
}
