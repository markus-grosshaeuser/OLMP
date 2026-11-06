package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.loans.LoanContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.loans.LoanManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/loan-management", layout = AdminLayout.class)
@PageTitle("Loan Management")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class LoanManagementPage extends LoanManagementView {
    public LoanManagementPage(LoanContext loanContext, UiContext uiContext) {
        super(loanContext, uiContext);
    }
}
