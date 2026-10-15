package com.grosshaeuser.olmp.ui.internal_ui.root_admin;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "root-admin", layout = RootAdminLayout.class)
@PageTitle("Root Admin Home")
@RolesAllowed(SecurityConstants.Roles.ROOT_ADMIN)
public class RootAdminHome extends VerticalLayout {
    public RootAdminHome() {
        add(new H1("Root Admin Home"));
    }
}
