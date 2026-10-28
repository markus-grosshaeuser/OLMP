package com.grosshaeuser.olmp.ui.internal_ui.shared.employees;

import com.grosshaeuser.olmp.employees.dto.EmployeeRegistrationDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
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

import java.util.HashSet;
import java.util.List;

public class EmployeeManagementView extends VerticalLayout implements ConfirmDelete {
    private final UiContext uiContext;

    private final EmployeeService employeeService;
    private final PasswordProperties passwordProperties;
    private final TextField searchField = new TextField("Search Employees");
    private final Grid<EmployeeDTO> employeeGrid = new Grid<>(EmployeeDTO.class, false);
    private final HashSet<String> assignableRoles;

    public EmployeeManagementView(EmployeeContext employeeContext, UiContext uiContext) {

        this.uiContext = uiContext;

        this.employeeService = employeeContext.getEmployeeService();
        this.passwordProperties = employeeContext.getPasswordProperties();

        this.assignableRoles = EmployeeMapper.mapRolesToRoleNames(employeeContext.getRoleService().getAssignableRoles());
        configureSearch();
        configureGrid();

        Button create = new Button("Create Employee", VaadinIcon.PLUS.create(),_ -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(searchField, create);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        toolbar.setWrap(true);
        add(new H2("Employee management"), toolbar, employeeGrid);
        setSizeFull();
        setFlexGrow(1, employeeGrid);
    }

    private void configureSearch() {
        searchField.setPlaceholder("Search ...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(_ -> refreshGrid());
    }

    private void configureGrid() {
        employeeGrid.addColumn(EmployeeDTO::lastName)
                .setHeader("Last Name")
                .setKey("lastName")
                .setSortProperty("lastName")
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addColumn(EmployeeDTO::firstName)
                .setHeader("First Name")
                .setKey("firstName")
                .setSortProperty("firstName")
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addColumn(EmployeeDTO::email)
                .setHeader("E-Mail")
                .setKey("email")
                .setSortProperty("email")
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addColumn(EmployeeDTO::creationTimestamp)
                .setHeader("Created")
                .setKey("createdAt")
                .setSortProperty("createdAt")
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addColumn(EmployeeDTO::lastModifiedTimestamp)
                .setHeader("Last Modified")
                .setKey("updatedAt")
                .setSortProperty("updatedAt")
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addColumn(employee -> String.join(", ", employee.roles()))
                .setHeader("Roles")
                .setKey("roles")
                .setSortable(false)
                .setAutoWidth(true)
                .setResizable(true);

        employeeGrid.addComponentColumn(employee -> {
            Button edit = new Button(
                    VaadinIcon.EDIT.create(),
                    _ -> employeeGrid.setDetailsVisible(employee, !employeeGrid.isDetailsVisible(employee))
            );
            edit.setAriaLabel("Edit " + employee.firstName() + " " + employee.lastName());
            edit.addThemeVariants(ButtonVariant.LUMO_ICON);
            Button delete = new Button(
                    VaadinIcon.TRASH.create(),
                    _ -> confirmDelete(employee)
            );
            delete.setAriaLabel("Delete " + employee.firstName() + " " + employee.lastName());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Span actionElements = new Span(edit, delete);
            actionElements.addClassName("grid-action-elements");
            return actionElements;
        })
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        employeeGrid.setSelectionMode(Grid.SelectionMode.NONE);
        employeeGrid.setDetailsVisibleOnClick(false);
        employeeGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        employeeGrid.addThemeVariants(GridVariant.ROW_STRIPES, GridVariant.WRAP_CELL_CONTENT);
        employeeGrid.setSizeFull();
        employeeGrid.setItemsPageable(this::fetchEmployee, _ -> countEmployee()).setIdentifierProvider(EmployeeDTO::id);
        employeeGrid.sort(List.of(new GridSortOrder<>(employeeGrid.getColumnByKey("lastName"), SortDirection.ASCENDING)));
        employeeGrid.setEmptyStateText("No employees found");
    }

    private List<EmployeeDTO> fetchEmployee(Pageable pageable) {
        try {
            return employeeService.getEmployees(searchField.getValue().strip(), pageable).getContent();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return List.of();
        }
    }

    private long countEmployee() {
        try {
            return employeeService.countEmployees(searchField.getValue().strip());
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return 0;
        }
    }

    private Component createEditor(EmployeeDTO employee) {
        try {
            return employeeService.getDetailedEmployeeById(employee.id())
                    .<Component>map(dto -> new EmployeeEditComponent(
                            dto,
                            assignableRoles,
                            this::updateEmployee,
                            uiContext::handleException,
                            () -> employeeGrid.setDetailsVisible(employee, false),
                            passwordProperties)
                    )
                    .orElseGet(() -> unavailableDetails("This employee does not exist."));
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
            return unavailableDetails("Employee details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void openCreateDialog() {
        try {
            new EmployeeCreateDialog(
                    assignableRoles,
                    this::createEmployee,
                    uiContext::handleException,
                    passwordProperties
            ).open();
        } catch (RuntimeException exception) {
            uiContext.handleException(exception);
        }

    }

    private void createEmployee(EmployeeRegistrationDTO dto) {
        try {
            employeeService.createEmployee(dto).ifPresentOrElse(
                    _ -> {
                        uiContext.showSuccessNotification("New employee account for " +
                                dto.getFirstName() + " " + dto.getLastName() +
                                " was created successfully.");
                        if (searchField.isEmpty()) {
                            refreshGrid();
                        } else {
                            searchField.clear();
                        }
                    },
                    () -> uiContext.handleException(new RuntimeException("Employee creation failed."))
            );
        } catch (Exception exception) {
            uiContext.handleException(new RuntimeException("An unexpected error occurred while creating a new employee."));
        }
    }

    private EmployeeEditDTO updateEmployee(EmployeeEditDTO dto) {
        return employeeService.updateEmployee(dto).map(result -> {
            uiContext.showSuccessNotification("User account for " +
                    result.getFirstName() + " " + result.getLastName() +
                    " was updated successfully.");
            employeeGrid.getDataProvider().refreshAll();
            return result;
        }).orElse(null);
    }

    private void confirmDelete(EmployeeDTO user) {
        ConfirmDialog dialog = getConfirmDialog(
                "Delete user account?",
                new Html("<p>You are about to delete the user account of:<br /><em> " +
                        user.firstName() + " " + user.lastName() +
                        "</em><br />This action cannot be undone!</p>"),
                _ -> delete(user)
                );
        dialog.open();
    }

    private void delete(EmployeeDTO user) {
        employeeService.deleteEmployee(user.id());
        refreshGrid();
    }

    private void refreshGrid() {
        employeeGrid.getDataProvider().refreshAll();
        employeeGrid.scrollToIndex(0);
    }
}
