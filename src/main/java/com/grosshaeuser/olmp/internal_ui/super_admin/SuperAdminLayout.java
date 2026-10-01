package com.grosshaeuser.olmp.internal_ui.super_admin;

import com.grosshaeuser.olmp.internal_ui.MainLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("SUPER_ADMIN")
public class SuperAdminLayout extends MainLayout {
    public SuperAdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        super.header.addToMiddle(new Paragraph("Logged in as ..."));
    }
}
