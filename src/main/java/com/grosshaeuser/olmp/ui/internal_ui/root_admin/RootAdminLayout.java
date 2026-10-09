package com.grosshaeuser.olmp.ui.internal_ui.root_admin;

import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ROOT_ADMIN")
public class RootAdminLayout extends MainLayout {
    public RootAdminLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

    }
}
