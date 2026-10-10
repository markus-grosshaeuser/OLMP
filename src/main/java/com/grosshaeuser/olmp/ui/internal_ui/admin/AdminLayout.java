package com.grosshaeuser.olmp.ui.internal_ui.admin;

import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.EmployeeManagementPage;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.MemberManagementPage;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
public class AdminLayout extends MainLayout {
    public AdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

        this.sideNav.addItem(new SideNavItem("Employee Management", EmployeeManagementPage.class, VaadinIcon.USER.create()));
        this.sideNav.addItem(new SideNavItem("Member Management", MemberManagementPage.class, VaadinIcon.USER_STAR.create()));
    }
}
