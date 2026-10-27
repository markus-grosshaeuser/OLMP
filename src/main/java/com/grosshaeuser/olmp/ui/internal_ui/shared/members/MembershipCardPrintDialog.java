package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.service.MembershipCardPrintService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Objects;

public class MembershipCardPrintDialog extends Dialog implements Printing{
    private final MembershipCardPrintService printService;
    private final Span error = new Span();
    private final Button print = new Button("Yes", _ -> print());

    private final MemberEditDTO member;
    private final MembershipEditDTO membership;

    public MembershipCardPrintDialog(MembershipCardPrintService printService, MemberEditDTO member, MembershipEditDTO membership) {
        this.printService = Objects.requireNonNull(printService, "printService must not be null");
        this.member = member;
        this.membership = membership;
        setHeaderTitle("Print Membership Card?");
        setWidth("450px");
        setMaxWidth("95vw");
        setCloseOnOutsideClick(false);
        error.getElement().setAttribute("role", "alert");
        error.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        error.setVisible(false);
        String question = "Print membership card for " + member.getFirstName() + " " + member.getLastName() + "?";
        add(new VerticalLayout(
                new Span("The Membership was created successfully. " + question),
                new Span("Your browser's print dialog will open. Choose your printer and print on A4 paper."), error));
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("No", _ -> close()), print);
    }

    private void print() {
        error.setVisible(false);
        print.setEnabled(true);
        try {
            String document = printService.createPrintDocument(member, membership);
            UI.getCurrentOrThrow().getPage().executeJs(jsPrintExpression, document).then(Boolean.class, _ -> close(), _ -> printFailed());
        } catch (RuntimeException e) {
            printFailed();
        }
    }

    private void printFailed() {
        error.setText("The membership was created successfully, but printing could not be started. Try Yes again or choose No.");
        error.setVisible(true);
        print.setEnabled(true);
    }
}
