package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.employees.EmployeeSelfServiceView;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/self-service", layout = EmployeeLayout.class)
@PageTitle("My Profile")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class SelfServiceEditPage extends EmployeeSelfServiceView {
    public SelfServiceEditPage(
            EmployeeService employeeService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        super(employeeService, passwordProperties, uiExceptionHandler, uiNotificationHandler);
    }
}
