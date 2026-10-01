package com.grosshaeuser.olmp.internal_ui.admin;

import com.grosshaeuser.olmp.internal_ui.MainLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
public class AdminLayout extends MainLayout {
    public AdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        super.header.addToMiddle(new Paragraph("Logged in as ..."));
    }
}
