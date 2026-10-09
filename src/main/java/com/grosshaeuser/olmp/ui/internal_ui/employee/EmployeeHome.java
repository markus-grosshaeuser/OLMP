package com.grosshaeuser.olmp.ui.internal_ui.employee;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff", layout = EmployeeLayout.class)
@PageTitle("Employee Home")
@RolesAllowed("STAFF")
public class EmployeeHome extends VerticalLayout {
    public EmployeeHome() {
        add(new H1("Employee Home"));
    }
}
