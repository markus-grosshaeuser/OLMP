package com.grosshaeuser.olmp.internal_ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

@RolesAllowed({"STAFF", "ADMIN", "SUPER_ADMIN"})
public abstract class MainLayout extends AppLayout {
    protected HorizontalLayout header;

    public MainLayout(AuthenticationContext authenticationContext) {
        H1 logo = new H1("OLMP");
        logo.addClassName("logo");

        this.header = new HorizontalLayout();
        header.setWidthFull();
        header.addToStart(logo);

        authenticationContext.getAuthenticatedUser(UserDetails.class)
                .ifPresent(_ -> {
                    header.addToEnd(new Button("Logout", _ -> authenticationContext.logout()));
                    addToNavbar(header);
                });
    }
}
