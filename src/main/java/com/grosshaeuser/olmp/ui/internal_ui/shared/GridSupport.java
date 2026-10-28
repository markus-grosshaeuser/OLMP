package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class GridSupport {
    private GridSupport() {}

    public static <T> Span createGridActionElements(String ariaDescription, Grid<T> grid, T dto, ComponentEventListener<ClickEvent<Button>> confirmDelete) {
        Button edit = new Button(
                VaadinIcon.EDIT.create(),
                _ -> grid.setDetailsVisible(dto, !grid.isDetailsVisible(dto))
        );
        edit.setAriaLabel("Edit " + ariaDescription);
        edit.addThemeVariants(ButtonVariant.LUMO_ICON);
        Button delete = new Button(VaadinIcon.TRASH.create(), confirmDelete);
        delete.setAriaLabel("Delete " + ariaDescription);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Span actionElements = new Span(edit, delete);
        actionElements.addClassName("grid-action-elements");
        return actionElements;
    }
    
    public static <T> Grid<T> getPreconfiguredManagementGrid(Class<T> dtoClass, String noItemsText) {
        Grid<T> grid = new Grid<>(dtoClass, false);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setDetailsVisibleOnClick(false);
        grid.addThemeVariants(GridVariant.ROW_STRIPES, GridVariant.WRAP_CELL_CONTENT);
        grid.setSizeFull();
        grid.setEmptyStateText(noItemsText);
        return grid;
    }
}
