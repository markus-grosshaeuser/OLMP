package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class EmployeeManagementView extends VerticalLayout {

    private final EmployeeService employeeService;

    private final Grid<EmployeeDTO> employeeGrid;
    private final TextField searchField;


    public EmployeeManagementView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeGrid = getPreconfiguredGrid();

        this.searchField = getPreconfiguredSearchField();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.addToStart(searchField);

        this.add(toolbar);
        this.add(employeeGrid);

        this.setSizeFull();
    }

    private TextField getPreconfiguredSearchField() {
        TextField textField = new TextField();
        textField.setPlaceholder("Search ...");
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        textField.addValueChangeListener(
                _ ->
                        this.employeeGrid.getDataProvider().refreshAll()
        );
        return textField;
    }

    private Grid<EmployeeDTO> getPreconfiguredGrid() {
        Grid<EmployeeDTO> grid = new Grid<>();

        grid.addColumn(EmployeeDTO::lastName)
                .setHeader("Last Name")
                .setSortProperty("lastName")
                .setAutoWidth(true);

        grid.addColumn(EmployeeDTO::firstName)
                .setHeader("First Name")
                .setSortProperty("firstName")
                .setAutoWidth(true);

        grid.addColumn(EmployeeDTO::email)
                .setHeader("E-Mail")
                .setSortProperty("email")
                .setAutoWidth(true);

        grid.addColumn(EmployeeDTO::creationTimestamp)
                .setHeader("Created")
                .setSortProperty("createdAt")
                .setAutoWidth(true);

        grid.addColumn(EmployeeDTO::lastModifiedTimestamp)
                .setHeader("Last Modified")
                .setSortProperty("updatedAt")
                .setAutoWidth(true);

        grid.addColumn(EmployeeDTO::roles)
                .setHeader("Roles")
                .setSortable(false)
                .setAutoWidth(true);

        grid.setItemsPageable(
                pageable ->
                        employeeService.getEmployees(searchField.getValue().trim(),pageable).getContent()
        );

        grid.setDetailsVisibleOnClick(false);
        grid.setSizeFull();
        return grid;
    }

}
