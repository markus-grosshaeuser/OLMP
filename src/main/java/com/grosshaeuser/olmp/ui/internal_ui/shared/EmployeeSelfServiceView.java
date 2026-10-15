package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.components.EmployeeSelfServiceEditComponent;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiNotificationHandler;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class EmployeeSelfServiceView extends VerticalLayout {
    private final EmployeeService employeeService;
    private final UiNotificationHandler uiNotificationHandler;

    public EmployeeSelfServiceView(
            EmployeeService employeeService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        this.employeeService = employeeService;
        this.uiNotificationHandler = uiNotificationHandler;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("My Profile"));

        employeeService.getCurrentEmployee()
                .map(employee -> new EmployeeSelfServiceEditComponent(
                        employee,
                        this::updateCurrentEmployee,
                        uiExceptionHandler::handleException,
                        passwordProperties
                ))
                .ifPresentOrElse(
                        this::add,
                        () -> uiExceptionHandler.showError("No employee account was found for the current user.")
                );
    }

    private EmployeeEditDTO updateCurrentEmployee(EmployeeEditDTO dto) {
        return employeeService.updateCurrentEmployee(dto).map(updatedDto -> {
            uiNotificationHandler.showSuccessNotification("Your profile was updated.");
            return updatedDto;
        }).orElseThrow(() -> new IllegalStateException("Could not update current employee."));
    }
}