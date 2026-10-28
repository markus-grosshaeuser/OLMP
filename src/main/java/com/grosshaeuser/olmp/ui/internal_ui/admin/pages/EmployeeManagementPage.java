package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.EmployeeContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.employees.EmployeeManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/employee-management", layout = AdminLayout.class)
@PageTitle("Employee Management")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class EmployeeManagementPage extends EmployeeManagementView {
    public EmployeeManagementPage(EmployeeContext employeeContext, UiContext uiContext) {
        super(employeeContext, uiContext);
    }
}
