package com.grosshaeuser.olmp.ui.internal_ui.root_admin;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.grosshaeuser.olmp.ui.internal_ui.root_admin.pages.SelfServiceEditPage;
import com.grosshaeuser.olmp.ui.internal_ui.root_admin.pages.EmployeeManagementPage;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(SecurityConstants.Roles.ROOT_ADMIN)
public class RootAdminLayout extends MainLayout {
    public RootAdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

        this.sideNav.addItem(new SideNavItem("My Profile", SelfServiceEditPage.class, VaadinIcon.USER_CARD.create()));
        this.sideNav.addItem(new SideNavItem("Employee Management", EmployeeManagementPage.class, VaadinIcon.USER.create()));
    }
}
