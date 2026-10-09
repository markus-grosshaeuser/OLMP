package com.grosshaeuser.olmp.ui.internal_ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

@RolesAllowed({"STAFF", "ADMIN", "ROOT_ADMIN"})
public abstract class MainLayout extends AppLayout {
    protected HorizontalLayout header;
    protected SideNav sideNav;

    public MainLayout(AuthenticationContext authenticationContext) {
        H1 logo = new H1("OLMP");
        H2 title = new H2("The Open Library Management Platform");

        this.header = new HorizontalLayout();
        header.addClassName("header");
        header.setWidthFull();
        header.addToStart(logo, title);

        authenticationContext.getAuthenticatedUser(UserDetails.class)
                .ifPresent(principal -> {
                    Paragraph loggedInAs = new Paragraph("Logged in as " + principal.getUsername());
                    Button logOutButton = new Button("Logout", _ -> authenticationContext.logout());
                    header.addToEnd(loggedInAs, logOutButton);
                    addToNavbar(header);
                });

        this.sideNav = new SideNav();
        sideNav.addClassName("side-nav");
        addToDrawer(sideNav);
    }
}
