package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jspecify.annotations.NonNull;

public interface Structurable {

    @FunctionalInterface
    interface Closeable {
        void close();
    }

    default Div createDialogHeader(String title, String subtitle, Closeable closeable) {
        H2 dialogTitle = new H2(title);
        dialogTitle.addClassName("dialog-title");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addClickListener(_ -> closeable.close());

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addToStart(dialogTitle);
        topRow.addToEnd(closeButton);
        return createCommonHeaderPart(subtitle, topRow);
    }

    default Div createComponentHeader(String title, String subtitle) {
        H2 dialogTitle = new H2(title);
        dialogTitle.addClassName("dialog-title");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addToStart(dialogTitle);
        return createCommonHeaderPart(subtitle, topRow);
    }

    @NonNull
    private Div createCommonHeaderPart(String subtitle, HorizontalLayout topRow) {
        topRow.setAlignItems(FlexComponent.Alignment.START);
        topRow.addClassName("dialog-top-row");

        Span dialogSubtitle = new Span(subtitle);
        dialogSubtitle.addClassName("dialog-subtitle");

        Div dialogHeader = new Div(topRow, dialogSubtitle);
        dialogHeader.addClassName("dialog-header");

        dialogHeader.setWidthFull();
        return dialogHeader;
    }

    default Div createSection(String title, String description, Component... components) {
        H3 sectionTitle = new H3(title);
        sectionTitle.addClassName("dialog-section-title");

        Span sectionDescription = new Span(description);
        sectionDescription.addClassName("dialog-section-description");

        Div sectionHeader = new Div(sectionTitle, sectionDescription);
        sectionHeader.addClassName("dialog-section-header");

        Div dialogComponents = new Div(components);
        dialogComponents.addClassName("dialog-section-components");

        Div section = new Div();
        section.addClassName("dialog-section");

        section.add(sectionHeader);
        section.add(dialogComponents);

        section.setWidthFull();
        return section;
    }

    default HorizontalLayout createActionBar(Button[] primaryButtons, Button[] secondaryButtons, Button[] tertiaryButtons)
    {
        Div primaryActions = new Div(primaryButtons);
        primaryActions.addClassName("dialog-action-bar-primary");

        Div secondaryActions = new Div(secondaryButtons);
        secondaryActions.addClassName("dialog-action-bar-secondary");

        Div tertiaryActions = new Div(tertiaryButtons);
        tertiaryActions.addClassName("dialog-action-bar-tertiary");

        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.addClassName("dialog-action-bar");
        actionBar.setWidthFull();

        actionBar.addToStart(primaryActions);
        actionBar.addToMiddle(secondaryActions);
        actionBar.addToEnd(tertiaryActions);

        return actionBar;
    }
}
