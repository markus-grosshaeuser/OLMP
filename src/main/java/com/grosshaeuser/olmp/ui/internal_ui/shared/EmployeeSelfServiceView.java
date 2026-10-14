package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employee.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.components.EmployeeSelfServiceEditComponent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;


public class EmployeeSelfServiceView extends VerticalLayout {
    private final EmployeeService employeeService;

    public EmployeeSelfServiceView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("My Profile"));

        employeeService.getCurrentEmployee()
                .map(employee -> new EmployeeSelfServiceEditComponent(
                        employee,
                        this::updateCurrentEmployee,
                        this::handleException
                ))
                .ifPresentOrElse(
                        this::add,
                        () -> {
                            Notification notification = new Notification("No employee account was found for the current user.");
                            notification.setPosition(Notification.Position.MIDDLE);
                            notification.addThemeVariants(NotificationVariant.ERROR);
                            notification.setDuration(4000);
                            notification.open();
                        }
                );
    }

    private EmployeeEditDTO updateCurrentEmployee(EmployeeEditDTO dto) {
        return employeeService.updateCurrentEmployee(dto)
                .orElseThrow(() -> new IllegalStateException("Could not update current employee."));
    }

    private void handleException(RuntimeException e) {
        switch (e) {
            case OptimisticLockingFailureException _ -> {
                var notification = new Notification("Someone has edited your employee data. Please refresh and try again.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.WARNING);
                notification.setDuration(3000);
                notification.open();
            }
            case AccessDeniedException _, IllegalArgumentException _, DataIntegrityViolationException _ -> {
                var notification = new Notification(e.getMessage());
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.ERROR);
                notification.setDuration(4000);
                notification.open();
            }
            case null -> {
            }
            default -> throw e;
        }
    }
}