package com.grosshaeuser.olmp.ui.internal_ui.shared.employees;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class EmployeeSelfServiceView extends VerticalLayout {

    private final UiContext uiContext;

    private final EmployeeService employeeService;


    public EmployeeSelfServiceView(EmployeeContext employeeContext, UiContext uiContext) {

        this.uiContext = uiContext;

        this.employeeService = employeeContext.getEmployeeService();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("My Profile"));

        employeeService.getCurrentEmployee()
                .map(employee -> new EmployeeSelfServiceEditComponent(
                        employee,
                        this::updateCurrentEmployee,
                        uiContext::handleException,
                        employeeContext.getPasswordProperties()
                ))
                .ifPresentOrElse(
                        this::add,
                        () -> uiContext.showError("No employee account was found for the current user.")
                );
    }

    private EmployeeEditDTO updateCurrentEmployee(EmployeeEditDTO dto) {
        return employeeService.updateCurrentEmployee(dto).map(updatedDto -> {
            uiContext.showSuccessNotification("Your profile was updated.");
            return updatedDto;
        }).orElseThrow(() -> new IllegalStateException("Could not update current employee."));
    }
}