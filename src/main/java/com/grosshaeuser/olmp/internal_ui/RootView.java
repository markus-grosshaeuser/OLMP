package com.grosshaeuser.olmp.internal_ui;

import com.grosshaeuser.olmp.internal_ui.admin.AdminHome;
import com.grosshaeuser.olmp.internal_ui.staff.StaffHome;
import com.grosshaeuser.olmp.internal_ui.super_admin.SuperAdminHome;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route("/")
@RolesAllowed({"STAFF", "ADMIN", "SUPER_ADMIN"})
public class RootView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthenticationContext authenticationContext;

    public RootView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.hasRole("SUPER_ADMIN")) {
            event.getUI().navigate(SuperAdminHome.class);
        } else if (authenticationContext.hasRole("ADMIN")) {
            event.getUI().navigate(AdminHome.class);
        } else if (authenticationContext.hasRole("STAFF")) {
            event.getUI().navigate(StaffHome.class);
        }
    }
}
