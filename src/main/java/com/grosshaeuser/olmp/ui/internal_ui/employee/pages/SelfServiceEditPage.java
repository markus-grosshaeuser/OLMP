package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeSelfServiceView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/self-service", layout = EmployeeLayout.class)
@PageTitle("My Profile")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class SelfServiceEditPage extends EmployeeSelfServiceView {
    public SelfServiceEditPage(EmployeeService employeeService) {
        super(employeeService);
    }
}
