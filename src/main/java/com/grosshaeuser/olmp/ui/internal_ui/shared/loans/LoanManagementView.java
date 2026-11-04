package com.grosshaeuser.olmp.ui.internal_ui.shared.loans;

import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.LoanSummaryDto;
import com.grosshaeuser.olmp.ui.internal_ui.shared.GridSupport;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LoanManagementView extends VerticalLayout {
    private final LoanContext context;
    private final TextField search = new TextField("Search loans");
    private final ComboBox<LoanStatus> status = new ComboBox<>("Status");
    private final Grid<LoanSummaryDto> grid = GridSupport.getPreconfiguredManagementGrid(LoanSummaryDto.class, "No loans found");

    public LoanManagementView(LoanContext loanContext) {
        context = Objects.requireNonNull(loanContext, "loanContext");
        search.setPlaceholder("Member, membership number, title or copy code");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setValueChangeTimeout(300);
        search.addValueChangeListener(_ -> refreshGrid());

        status.setItems(LoanStatus.values());
        status.setItemLabelGenerator(LoanManagementView::statusLabel);
        status.setPlaceholder("All statuses");
        status.setClearButtonVisible(true);
        status.addValueChangeListener(_ -> refreshGrid());

        Button checkout = new Button("Check out", VaadinIcon.PLUS.create(), _ ->
                new LoanBatchDialog(context, true, this::refreshGrid).open());
        checkout.addThemeVariants(ButtonVariant.PRIMARY);
        Button returns = new Button("Return copies", VaadinIcon.ARROW_BACKWARD.create(), _ ->
                new LoanBatchDialog(context, false, this::refreshGrid).open());
        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), _ -> refreshGrid());
        HorizontalLayout toolbar = new HorizontalLayout(search, status, checkout, returns, refresh);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, search);
        search.setMinWidth("16em");

        configureGrid();
        add(new H2("Loan management"), toolbar, grid);
        setSizeFull();
        setFlexGrow(1, grid);
    }

    private void configureGrid() {
        grid.addColumn(LoanSummaryDto::memberName).setHeader("Member")
                .setSortProperty("member.lastName").setResizable(true);
        grid.addColumn(LoanSummaryDto::mediaTitle).setHeader("Title")
                .setSortProperty("inventoryItem.media.title").setResizable(true).setFlexGrow(2);
        grid.addColumn(LoanSummaryDto::inventoryNumber).setHeader("Inventory number")
                .setSortProperty("inventoryItem.inventoryNumber").setResizable(true);
        grid.addColumn(loan -> formatDate(loan.dueDate())).setHeader("Due")
                .setKey("dueDate").setSortProperty("dueDate").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(loan -> {
            Span badge = new Span(statusLabel(loan.status()));
            String theme = switch (loan.status()) {
                case OVERDUE, LOST -> "badge error";
                case RETURNED -> "badge success";
                case CANCELLED -> "badge contrast";
                default -> "badge";
            };
            badge.getElement().getThemeList().addAll(List.of(theme.split(" ")));
            return badge;
        }).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(loan -> new Button(VaadinIcon.EDIT.create(),
                        _ -> grid.setDetailsVisible(loan, !grid.isDetailsVisible(loan))))
                .setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        grid.setItemsPageable(this::fetchLoans, _ -> countLoans()).setIdentifierProvider(LoanSummaryDto::id);
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("dueDate"), SortDirection.ASCENDING)));
    }

    private List<LoanSummaryDto> fetchLoans(Pageable pageable) {
        try {
            return context.getLoanService().findLoans(search.getValue().strip(), status.getValue(), pageable);
        } catch (RuntimeException exception) {
            context.getUiContext().handleException(exception);
            return List.of();
        }
    }

    private int countLoans() {
        try {
            return Math.toIntExact(context.getLoanService().countLoans(search.getValue().strip(), status.getValue()));
        } catch (RuntimeException exception) {
            context.getUiContext().handleException(exception);
            return 0;
        }
    }

    private Component createEditor(LoanSummaryDto loan) {
        try {
            return context.getLoanService().findLoan(loan.id())
                    .<Component>map(details -> new LoanEditComponent(context, details,
                            () -> {
                                grid.setDetailsVisible(loan, false);
                                grid.getDataProvider().refreshAll();
                            },
                            () -> grid.setDetailsVisible(loan, false)))
                    .orElseGet(() -> unavailableDetails("This loan no longer exists."));
        } catch (RuntimeException exception) {
            context.getUiContext().handleException(exception);
            return unavailableDetails("Loan details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
        grid.scrollToIndex(0);
    }

    static String statusLabel(LoanStatus status) {
        String text = status.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static String formatDate(OffsetDateTime date) {
        return date == null ? "—" : date.atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}
