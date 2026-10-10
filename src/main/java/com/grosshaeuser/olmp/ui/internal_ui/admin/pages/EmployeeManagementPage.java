package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/employee-management", layout = AdminLayout.class)
@PageTitle("Employee Management")
@RolesAllowed("ADMIN")
public class EmployeeManagementPage extends EmployeeManagementView {
    public EmployeeManagementPage(EmployeeService employeeService) {
        super(employeeService);
    }
}
