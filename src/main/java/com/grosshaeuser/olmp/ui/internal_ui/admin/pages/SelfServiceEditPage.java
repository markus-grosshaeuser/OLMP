package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeSelfServiceView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value="admin/self-service", layout = AdminLayout.class)
@PageTitle("My Profile")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class SelfServiceEditPage extends EmployeeSelfServiceView {
    public SelfServiceEditPage(EmployeeService employeeService) {
        super(employeeService);
    }
}