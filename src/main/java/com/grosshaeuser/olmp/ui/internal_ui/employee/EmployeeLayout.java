package com.grosshaeuser.olmp.ui.internal_ui.employee;

import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.grosshaeuser.olmp.ui.internal_ui.employee.pages.MemberManagementPage;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("STAFF")
public class EmployeeLayout extends MainLayout {
    public EmployeeLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

        this.sideNav.addItem(new SideNavItem("Member Management", MemberManagementPage.class, VaadinIcon.USER.create()));
    }
}
