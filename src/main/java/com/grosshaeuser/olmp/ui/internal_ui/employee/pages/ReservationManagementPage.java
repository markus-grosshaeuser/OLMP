package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.reservations.ReservationContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.reservations.ReservationManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "employee/reservation-management", layout = EmployeeLayout.class)
@PageTitle("Reservation Management")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class ReservationManagementPage extends ReservationManagementView {

    public ReservationManagementPage(ReservationContext reservationContext, UiContext uiContext) {
        super(reservationContext, uiContext);
    }
}
