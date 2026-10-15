package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.MemberManagementView;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.handler.UiNotificationHandler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/member-management", layout = EmployeeLayout.class)
@PageTitle("Member Management")
@RolesAllowed(SecurityConstants.Roles.STAFF)
public class MemberManagementPage extends MemberManagementView {
    public MemberManagementPage(
            MemberService memberService,
            MembershipService membershipService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        super(memberService, membershipService, passwordProperties, uiExceptionHandler, uiNotificationHandler);
    }
}
