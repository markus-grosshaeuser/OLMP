package com.grosshaeuser.olmp.ui.internal_ui.shared.reservations;

import com.grosshaeuser.olmp.reservations.ReservationStatus;
import com.grosshaeuser.olmp.reservations.dto.ReservationSummaryDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.GridSupport;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReservationManagementView extends VerticalLayout {
    private final ReservationContext reservationContext;
    private final UiContext uiContext;
    private final TextField search = new TextField("Search reservations");
    private final ComboBox<ReservationStatus> status = new ComboBox<>("Status");
    private final Grid<ReservationSummaryDto> grid = GridSupport.getPreconfiguredManagementGrid(
            ReservationSummaryDto.class, "No reservations found");

    public ReservationManagementView(ReservationContext reservationContext, UiContext uiContext) {
        this.reservationContext = Objects.requireNonNull(reservationContext, "reservationContext");
        this.uiContext = Objects.requireNonNull(uiContext, "uiContext");
        search.setPlaceholder("Member, membership number or title");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setValueChangeTimeout(300);
        search.addValueChangeListener(_ -> refreshGrid());

        status.setItems(ReservationStatus.values());
        status.setItemLabelGenerator(ReservationManagementView::statusLabel);
        status.setPlaceholder("All statuses");
        status.setClearButtonVisible(true);
        status.addValueChangeListener(_ -> refreshGrid());

        Button create = new Button("New reservations", VaadinIcon.PLUS.create(), _ ->
                new ReservationBatchDialog(this.reservationContext, this.uiContext, this::refreshGrid).open());
        create.addThemeVariants(ButtonVariant.PRIMARY);
        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), _ -> refreshGrid());
        HorizontalLayout toolbar = new HorizontalLayout(search, status, create, refresh);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, search);
        search.setMinWidth("16em");

        configureGrid();
        add(new H2("Reservation management"), toolbar, grid);
        setSizeFull();
        setFlexGrow(1, grid);
    }

    private void configureGrid() {
        grid.addColumn(ReservationSummaryDto::memberName).setHeader("Member")
                .setSortProperty("member.lastName").setResizable(true);
        grid.addColumn(ReservationSummaryDto::mediaTitle).setHeader("Title")
                .setSortProperty("media.title").setResizable(true).setFlexGrow(2);
        grid.addColumn(reservation -> formatDate(reservation.reservationDate())).setHeader("Reserved")
                .setKey("reservationDate").setSortProperty("reservationDate").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(reservation -> {
            Span badge = new Span(statusLabel(reservation.status()));
            badge.getElement().getThemeList().add("badge");
            switch (reservation.status()) {
                case FULFILLED -> badge.getElement().getThemeList().add("success");
                case CANCELLED -> badge.getElement().getThemeList().add("contrast");
                case EXPIRED -> badge.getElement().getThemeList().add("error");
                default -> { }
            }
            return badge;
        }).setHeader("Status").setSortProperty("status").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(reservation -> {
            Button edit = new Button(VaadinIcon.EDIT.create(),
                    _ -> grid.setDetailsVisible(reservation, !grid.isDetailsVisible(reservation)));
            edit.setAriaLabel("Edit reservation for " + reservation.mediaTitle() + " by " + reservation.memberName());
            edit.addThemeVariants(ButtonVariant.LUMO_ICON);
            return edit;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        grid.setItemsPageable(this::fetchReservations, _ -> countReservations()).setIdentifierProvider(ReservationSummaryDto::id);
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("reservationDate"), SortDirection.ASCENDING)));
    }

    private List<ReservationSummaryDto> fetchReservations(Pageable pageable) {
        try {
            return reservationContext.getReservationService().findReservations(search.getValue().strip(), status.getValue(), pageable);
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return List.of();
        }
    }

    private int countReservations() {
        try {
            return Math.toIntExact(reservationContext.getReservationService().countReservations(search.getValue().strip(), status.getValue()));
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return 0;
        }
    }

    private Component createEditor(ReservationSummaryDto reservation) {
        try {
            return reservationContext.getReservationService().findReservation(reservation.id())
                    .<Component>map(details -> new ReservationEditComponent(reservationContext, uiContext, details,
                            () -> {
                                grid.setDetailsVisible(reservation, false);
                                grid.getDataProvider().refreshAll();
                            },
                            () -> grid.setDetailsVisible(reservation, false)))
                    .orElseGet(() -> unavailableDetails("This reservation no longer exists."));
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return unavailableDetails("Reservation details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
        grid.scrollToIndex(0);
    }

    static String statusLabel(ReservationStatus status) {
        String text = status.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static String formatDate(OffsetDateTime date) {
        return date == null ? "—" : date.atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}
