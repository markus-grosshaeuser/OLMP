package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.loans.LoanContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.loans.LoanManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/loan-management", layout = EmployeeLayout.class)
@PageTitle("Loan Management")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class LoanManagementPage extends LoanManagementView {
    public LoanManagementPage(LoanContext loanContext) {
        super(loanContext);
    }
}
