package com.grosshaeuser.olmp.internal_ui.admin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = AdminLayout.class)
@PageTitle("Admin Home")
@RolesAllowed("ADMIN")
public class AdminHome extends VerticalLayout {
    public AdminHome() {
        add(new H1("Admin Home"));
    }
}
