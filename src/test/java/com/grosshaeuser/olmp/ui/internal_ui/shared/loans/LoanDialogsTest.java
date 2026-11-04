package com.grosshaeuser.olmp.ui.internal_ui.shared.loans;

import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemDetailDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaReferenceDto;
import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.*;
import com.grosshaeuser.olmp.loans.service.LoanService;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanDialogsTest {
    private final LoanService service = mock(LoanService.class);
    private final UiContext ui = mock(UiContext.class);
    private final LoanContext context = new LoanContext(service, ui);
    private final AtomicInteger refreshes = new AtomicInteger();
    private UI currentUi;

    @BeforeEach
    void setUp() {
        currentUi = new UI();
        UI.setCurrent(currentUi);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void duplicateScansAreNotSubmittedTwiceAndFailedCheckoutCanBeRetried() {
        when(service.hasOverdueLoans("CARD-123")).thenReturn(true);
        when(service.findCopy("SCAN-1")).thenReturn(Optional.of(new LoanCopyDto(1L, "COPY-1", "Book")));
        when(service.createLoans(any())).thenThrow(new IllegalArgumentException("Membership expired"))
                .thenReturn(List.of());
        LoanBatchDialog dialog = new LoanBatchDialog(context, true, refreshes::incrementAndGet);
        dialog.open();
        field(dialog, "Membership number").setValue("CARD-123");
        assertThat(overdueWarning(dialog).isVisible()).isTrue();
        scan(dialog, "SCAN-1");
        scan(dialog, "SCAN-1");
        verify(ui).showWarning("This copy is already in the batch.");
        Button submit = button(dialog, "Check out copies (1)");
        submit.click();
        assertThat(dialog.isOpened()).isTrue();
        assertThat(submit.isEnabled()).isTrue();
        assertThat(refreshes).hasValue(0);
        submit.click();
        var dto = ArgumentCaptor.forClass(LoanBatchCreateDto.class);
        verify(service, times(2)).createLoans(dto.capture());
        assertThat(dto.getValue().getInventoryItemIds()).containsExactly(1L);
        assertThat(dto.getValue().getMembershipNumber()).isEqualTo("CARD-123");
        assertThat(dialog.isOpened()).isFalse();
        assertThat(refreshes).hasValue(1);
    }

    @Test
    void overdueWarningUpdatesWhenMembershipNumberChangesAndClearsWhenEmptied() {
        when(service.hasOverdueLoans("OVERDUE-CARD")).thenReturn(true);
        LoanBatchDialog dialog = new LoanBatchDialog(context, true, refreshes::incrementAndGet);
        TextField membership = field(dialog, "Membership number");
        Span warning = overdueWarning(dialog);
        assertThat(warning.isVisible()).isFalse();
        membership.setValue(" OVERDUE-CARD ");
        assertThat(warning.isVisible()).isTrue();
        assertThat(warning.getText()).isEqualTo("Warning: this member has overdue loans.");
        membership.setValue("OTHER-CARD");
        assertThat(warning.isVisible()).isFalse();
        membership.setValue("OVERDUE-CARD");
        assertThat(warning.isVisible()).isTrue();
        membership.clear();
        assertThat(warning.isVisible()).isFalse();
        verify(service, never()).hasOverdueLoans("");
    }

    @Test
    void failedOverdueCheckShowsUnavailableWarningInsteadOfPreviousMembersWarning() {
        when(service.hasOverdueLoans("OVERDUE-CARD")).thenReturn(true);
        when(service.hasOverdueLoans("OTHER-CARD")).thenThrow(new IllegalArgumentException("Lookup failed"));
        LoanBatchDialog dialog = new LoanBatchDialog(context, true, refreshes::incrementAndGet);
        TextField membership = field(dialog, "Membership number");
        membership.setValue("OVERDUE-CARD");
        membership.setValue("OTHER-CARD");
        assertThat(overdueWarning(dialog).isVisible()).isTrue();
        assertThat(overdueWarning(dialog).getText()).isEqualTo("Overdue loans could not be checked.");
        verify(ui).handleException(any(IllegalArgumentException.class));
    }

    @Test
    void typedButUnaddedCodePreventsSubmittingAnIncompleteBatch() {
        when(service.findCopy("SCAN-1")).thenReturn(Optional.of(new LoanCopyDto(1L, "COPY-1", "Book")));
        LoanBatchDialog dialog = new LoanBatchDialog(context, false, refreshes::incrementAndGet);
        scan(dialog, "SCAN-1");
        field(dialog, "Inventory number or barcode").setValue("SCAN-2");
        button(dialog, "Return copies (1)").click();
        verify(service, never()).returnLoans(any());
        verify(ui).handleException(any(IllegalArgumentException.class));
    }

    @Test
    void returningDoesNotRequireMembershipAndRefreshesAfterSuccess() {
        when(service.findCopy("SCAN-1")).thenReturn(Optional.of(new LoanCopyDto(1L, "COPY-1", "Book")));
        LoanBatchDialog dialog = new LoanBatchDialog(context, false, refreshes::incrementAndGet);
        dialog.open();
        assertThat(button(dialog, "Return copies (0)").isEnabled()).isFalse();
        scan(dialog, "SCAN-1");
        button(dialog, "Return copies (1)").click();
        var dto = ArgumentCaptor.forClass(LoanBatchReturnDto.class);
        verify(service).returnLoans(dto.capture());
        assertThat(dto.getValue().getInventoryItemIds()).containsExactly(1L);
        assertThat(dialog.isOpened()).isFalse();
        assertThat(refreshes).hasValue(1);
    }

    @Test
    void editingHistoricalLoanPreservesDatePrecisionAndSendsStaleEditToken() {
        OffsetDateTime due = OffsetDateTime.parse("2026-01-02T12:34:56.123456+01:00");
        LoanDto loan = loan(due);
        LoanEditComponent editor = new LoanEditComponent(context, loan, refreshes::incrementAndGet, () -> {
        });
        DateTimePicker picker = descendants(editor).filter(DateTimePicker.class::isInstance)
                .map(DateTimePicker.class::cast).findFirst().orElseThrow();
        assertThat(picker.isReadOnly()).isTrue();
        button(editor, "Save changes").click();
        var dto = ArgumentCaptor.forClass(LoanEditDto.class);
        verify(service).updateLoan(dto.capture());
        assertThat(dto.getValue().getDueDate()).isEqualTo(due);
        assertThat(dto.getValue().getExpectedUpdatedAt()).isEqualTo(loan.updatedAt());
        assertThat(dto.getValue().getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(refreshes).hasValue(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void loanEditorExpandsInsideGridAndCloseDiscardsChanges() {
        LoanDto loan = loan(OffsetDateTime.now());
        LoanSummaryDto summary = new LoanSummaryDto(loan.id(), "Ada Lovelace", "COPY-1", "Book", loan.dueDate(), loan.status());
        when(service.findLoan(loan.id())).thenReturn(Optional.of(loan));
        currentUi.getInternals().setSession(mock(VaadinSession.class, RETURNS_DEEP_STUBS));
        LoanManagementView view = new LoanManagementView(context);
        currentUi.add(view);
        Grid<LoanSummaryDto> grid = (Grid<LoanSummaryDto>) descendants(view).filter(Grid.class::isInstance)
                .findFirst().orElseThrow();
        ComponentRenderer<?, LoanSummaryDto> actions = (ComponentRenderer<?, LoanSummaryDto>) grid.getColumns().getLast().getRenderer();
        Button toggle = (Button) actions.createComponent(summary);
        toggle.click();
        assertThat(grid.isDetailsVisible(summary)).isTrue();
        Component editor = ReflectionTestUtils.invokeMethod(view, "createEditor", summary);
        assertThat(editor).isInstanceOf(LoanEditComponent.class);
        button(editor, "Close").click();
        assertThat(grid.isDetailsVisible(summary)).isFalse();
        verify(service, never()).updateLoan(any());
        toggle.click();
        assertThat(grid.isDetailsVisible(summary)).isTrue();
        toggle.click();
        assertThat(grid.isDetailsVisible(summary)).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void failedInlineSaveKeepsDetailsOpenAndSuccessfulRetryRefreshesGrid() {
        LoanDto loan = loan(OffsetDateTime.now());
        LoanSummaryDto summary = new LoanSummaryDto(loan.id(), "Ada Lovelace", "COPY-1", "Book", loan.dueDate(), loan.status());
        when(service.findLoan(loan.id())).thenReturn(Optional.of(loan));
        doThrow(new IllegalArgumentException("Save failed")).doNothing().when(service).updateLoan(any());
        currentUi.getInternals().setSession(mock(VaadinSession.class, RETURNS_DEEP_STUBS));
        LoanManagementView view = new LoanManagementView(context);
        currentUi.add(view);
        Grid<LoanSummaryDto> grid = (Grid<LoanSummaryDto>) descendants(view).filter(Grid.class::isInstance)
                .findFirst().orElseThrow();
        grid.setDetailsVisible(summary, true);
        grid.getDataProvider().addDataProviderListener(_ -> refreshes.incrementAndGet());
        Component editor = ReflectionTestUtils.invokeMethod(view, "createEditor", summary);
        Button save = button(editor, "Save changes");
        save.click();
        assertThat(grid.isDetailsVisible(summary)).isTrue();
        assertThat(save.isEnabled()).isTrue();
        assertThat(refreshes).hasValue(0);
        verify(ui).handleException(any(IllegalArgumentException.class));
        save.click();
        assertThat(grid.isDetailsVisible(summary)).isFalse();
        assertThat(refreshes).hasValue(1);
        verify(service, times(2)).updateLoan(any());
    }

    private LoanDto loan(OffsetDateTime due) {
        MemberDTO member = new MemberDTO(1L, "Lovelace", "Ada", null, null, null);
        InventoryItemDetailDto copy = new InventoryItemDetailDto(1L, new MediaReferenceDto(1L, "Book"),
                "COPY-1", "SCAN-1", null, null, null, null, null, null, null, null);
        return new LoanDto(1L, member, copy, null, due.minusDays(28), due, due.minusDays(2),
                LoanStatus.RETURNED, "", due.minusDays(28), due.minusDays(2));
    }

    private void scan(Dialog dialog, String code) {
        field(dialog, "Inventory number or barcode").setValue(code);
        button(dialog, "Add copy").click();
    }

    private TextField field(Dialog dialog, String label) {
        return descendants(dialog).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> label.equals(field.getLabel())).findFirst().orElseThrow();
    }

    private Span overdueWarning(Dialog dialog) {
        return descendants(field(dialog, "Membership number").getHelperComponent())
                .filter(Span.class::isInstance).map(Span.class::cast)
                .filter(span -> "status".equals(span.getElement().getAttribute("role"))).findFirst().orElseThrow();
    }

    private Button button(Component component, String text) {
        Stream<Component> footer = component instanceof Dialog dialog ? dialog.getFooter().getElement().getChildren()
                .flatMap(element -> element.getComponent().stream()).flatMap(this::descendants) : Stream.empty();
        return Stream.concat(descendants(component), footer)
                .filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> text.equals(button.getText())).findFirst().orElseThrow();
    }

    private Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::descendants));
    }
}
