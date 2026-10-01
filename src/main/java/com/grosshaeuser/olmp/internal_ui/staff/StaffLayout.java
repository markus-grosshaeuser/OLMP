package com.grosshaeuser.olmp.internal_ui.staff;

import com.grosshaeuser.olmp.internal_ui.MainLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("STAFF")
public class StaffLayout extends MainLayout {
    public StaffLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        super.header.addToMiddle(new Paragraph("Logged in as ..."));
    }
}
