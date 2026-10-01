package com.grosshaeuser.olmp.internal_ui.staff;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff", layout = StaffLayout.class)
@PageTitle("Staff Home")
@RolesAllowed("STAFF")
public class StaffHome extends VerticalLayout {
    public StaffHome() {
        add(new H1("Staff Home"));
    }
}
