package com.grosshaeuser.olmp.ui.internal_ui;

import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminHome;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeHome;
import com.grosshaeuser.olmp.ui.internal_ui.root_admin.RootAdminHome;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@Route("/")
@RolesAllowed({"STAFF", "ADMIN", "ROOT_ADMIN"})
public class RootView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthenticationContext authenticationContext;

    public RootView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.hasRole("ROOT_ADMIN")) {
            event.rerouteTo(RootAdminHome.class);
        } else if (authenticationContext.hasRole("ADMIN")) {
            event.rerouteTo(AdminHome.class);
        } else if (authenticationContext.hasRole("STAFF")) {
            event.rerouteTo(EmployeeHome.class);
        }
        else {
            event.rerouteToError(AccessDeniedException.class);
        }
    }
}
