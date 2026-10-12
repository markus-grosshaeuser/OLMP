package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employee.dto.EmployeeCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.mapper.RoleMapper;
import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.employee.service.RoleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;

public class EmployeeManagementView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final RoleService roleService;

    private final Grid<EmployeeDTO> employeeGrid;
    private final TextField searchField;


    public EmployeeManagementView(EmployeeService employeeService, RoleService roleService) {
        this.employeeService = employeeService;
        this.roleService = roleService;
        this.employeeGrid = getPreconfiguredGrid();

        this.searchField = getPreconfiguredSearchField();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.addToStart(searchField);
        toolbar.addToEnd(getPreconfiguredCreateEmployeeButton());

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

    private Button getPreconfiguredCreateEmployeeButton() {
        Button button = new Button("Create New Employee");
        button.addClickListener(
                _ -> {
                    HashSet<String> roles = RoleMapper.mapToStringHashSet(roleService.getAssignableRoles());
                    new EmployeeCreateDialog(roles, this::saveNewEmployee, this::handleException).open();
                }
        );
        return button;
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
                .setKey("createdAt")
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

    private void saveNewEmployee(EmployeeCreateDTO employeeCreateDTO) {
        employeeService.createEmployee(employeeCreateDTO).ifPresent(employee -> {
            searchField.clear();
            employeeGrid.getDataProvider().refreshAll();
            employeeGrid.sort(GridSortOrder.desc(employeeGrid.getColumnByKey("createdAt")).build());
            employeeGrid.select(employee);
            employeeGrid.scrollToIndex(0);
        });
    }

    private void handleException(RuntimeException e) {
        switch (e) {
            case OptimisticLockingFailureException _ -> {
                var notification = new Notification("Someone has edited the same Employee. Please refresh and try again.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.WARNING);
                notification.setDuration(3000);
                notification.open();
            }
            case AccessDeniedException _, IllegalArgumentException _ -> {
                var notification = new Notification(e.getMessage());
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.ERROR);
                notification.setDuration(4000);
                notification.open();
            }
            case null -> {}
            default -> throw e;
        }
    }
}
