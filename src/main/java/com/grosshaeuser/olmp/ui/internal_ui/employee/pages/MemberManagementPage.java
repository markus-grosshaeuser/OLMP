package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.MemberContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.grosshaeuser.olmp.ui.internal_ui.shared.members.MemberManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/member-management", layout = EmployeeLayout.class)
@PageTitle("Member Management")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class MemberManagementPage extends MemberManagementView {
    public MemberManagementPage(MemberContext memberContext, UiContext uiContext) {
        super(memberContext, uiContext);
    }
}
