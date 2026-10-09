package com.grosshaeuser.olmp.ui.internal_ui.employee;

import com.grosshaeuser.olmp.ui.internal_ui.MainLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("STAFF")
public class EmployeeLayout extends MainLayout {
    public EmployeeLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);

    }
}
