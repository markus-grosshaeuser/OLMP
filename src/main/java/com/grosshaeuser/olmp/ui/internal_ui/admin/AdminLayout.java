package com.grosshaeuser.olmp.ui.internal_ui.admin;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.EmployeeManagementPage;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.MemberManagementPage;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.SelfServiceEditPage;
import com.grosshaeuser.olmp.ui.internal_ui.admin.pages.MediaManagementPage;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class AdminLayout extends MainLayout {
    public AdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

        SideNav people = new SideNav("People");
        people.setCollapsible(true);
        people.addItem(new SideNavItem("My Profile", SelfServiceEditPage.class, VaadinIcon.USER_CARD.create()));
        people.addItem(new SideNavItem("Employee Management", EmployeeManagementPage.class, VaadinIcon.USER.create()));
        people.addItem(new SideNavItem("Member Management", MemberManagementPage.class, VaadinIcon.USER_STAR.create()));

        SideNav inventory = new SideNav("Inventory");
        inventory.setCollapsible(true);
        inventory.addItem(new SideNavItem("Media Management", MediaManagementPage.class, VaadinIcon.FOLDER_OPEN.create()));

        addToDrawer(people, inventory);
    }
}
