package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employees.dto.EmployeeRegistrationDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.employees.service.RoleService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.components.EmployeeCreateDialog;
import com.grosshaeuser.olmp.ui.internal_ui.shared.components.EmployeeEditComponent;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiNotificationHandler;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.HashSet;

public class EmployeeManagementView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final PasswordProperties passwordProperties;

    private final UiExceptionHandler uiExceptionHandler;
    private final UiNotificationHandler uiNotificationHandler;

    private final Grid<EmployeeDTO> employeeGrid;
    private final TextField searchField;


    public EmployeeManagementView(
            EmployeeService employeeService,
            RoleService roleService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        this.employeeService = employeeService;
        this.roleService = roleService;
        this.passwordProperties = passwordProperties;

        this.uiExceptionHandler = uiExceptionHandler;
        this.uiNotificationHandler = uiNotificationHandler;

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
        textField.setClearButtonVisible(true);
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
                    HashSet<String> roles = EmployeeMapper.mapRolesToRoleNames(roleService.getAssignableRoles());
                    new EmployeeCreateDialog(roles, this::saveNewEmployee, uiExceptionHandler::handleException, passwordProperties).open();
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

        grid.setDetailsVisibleOnClick(true);
        grid.setItemDetailsRenderer(createEmployeeEditRenderer());
        grid.setSizeFull();
        return grid;
    }

    private Renderer<EmployeeDTO> createEmployeeEditRenderer() {
        return new ComponentRenderer<>(element -> employeeService
                .getDetailedEmployeeById(element.id())
                .map(employeeEditDTO -> new EmployeeEditComponent(
                        employeeEditDTO,
                        EmployeeMapper.mapRolesToRoleNames(roleService.getAssignableRoles()),
                        this::updateEmployee,
                        this::deleteEmployee,
                        uiExceptionHandler::handleException,
                        passwordProperties)
                )
                .orElse(null)
        );
    }

    private EmployeeEditDTO updateEmployee(EmployeeEditDTO dto) {
        return employeeService.updateEmployee(dto).map(result -> {
            uiNotificationHandler.showSuccessNotification("User account for " + result.getFirstName() + " " + result.getLastName() + " was updated successfully.");
            employeeGrid.getDataProvider().refreshAll();
            return result;
        }).orElse(null);
    }

    private void deleteEmployee(EmployeeEditDTO dto) {
        employeeService.deleteEmployee(dto.getId());
        employeeGrid.getDataProvider().refreshAll();
    }

    private void saveNewEmployee(EmployeeRegistrationDTO employeeRegistrationDTO) {
        employeeService.createEmployee(employeeRegistrationDTO).ifPresent(employee -> {
            uiNotificationHandler.showSuccessNotification("New user account for " + employee.firstName() + " " + employee.lastName() + " was created successfully.");
            searchField.clear();
            employeeGrid.getDataProvider().refreshAll();
            employeeGrid.sort(GridSortOrder.desc(employeeGrid.getColumnByKey("createdAt")).build());
            employeeGrid.select(employee);
            employeeGrid.scrollToIndex(0);
        });
    }

}
