package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.employee.service.RoleService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/employee-management", layout = AdminLayout.class)
@PageTitle("Employee Management")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class EmployeeManagementPage extends EmployeeManagementView {
    public EmployeeManagementPage(EmployeeService employeeService, RoleService roleService) {
        super(employeeService, roleService);
    }
}
