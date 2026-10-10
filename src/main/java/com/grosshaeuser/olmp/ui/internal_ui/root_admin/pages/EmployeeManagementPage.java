package com.grosshaeuser.olmp.ui.internal_ui.root_admin.pages;

import com.grosshaeuser.olmp.employee.service.EmployeeService;
import com.grosshaeuser.olmp.ui.internal_ui.root_admin.RootAdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "root-admin/employee-management", layout = RootAdminLayout.class)
@PageTitle("Employee Management")
@RolesAllowed("ROOT_ADMIN")
public class EmployeeManagementPage extends EmployeeManagementView {
    public EmployeeManagementPage(EmployeeService employeeService) {
        super(employeeService);
    }
}
