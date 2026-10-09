package com.grosshaeuser.olmp.ui.internal_ui.admin;

import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
public class AdminLayout extends MainLayout {
    public AdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

    }
}
