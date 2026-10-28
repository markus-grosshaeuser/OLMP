package com.grosshaeuser.olmp.ui.internal_ui.root_admin.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.root_admin.RootAdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.employees.EmployeeContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.employees.EmployeeManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "root-admin/employee-management", layout = RootAdminLayout.class)
@PageTitle("Employee Management")
@RolesAllowed(SecurityConstants.Roles.ROOT_ADMIN)
public class EmployeeManagementPage extends EmployeeManagementView {
    public EmployeeManagementPage(EmployeeContext employeeContext, UiContext uiContext) {
        super(employeeContext, uiContext);
    }
}
