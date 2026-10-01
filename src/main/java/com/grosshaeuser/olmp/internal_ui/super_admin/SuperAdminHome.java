package com.grosshaeuser.olmp.internal_ui.super_admin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "super-admin", layout = SuperAdminLayout.class)
@PageTitle("Super Admin Home")
@RolesAllowed("SUPER_ADMIN")
public class SuperAdminHome extends VerticalLayout {
    public SuperAdminHome() {
        add(new H1("Super Admin Home"));
    }
}