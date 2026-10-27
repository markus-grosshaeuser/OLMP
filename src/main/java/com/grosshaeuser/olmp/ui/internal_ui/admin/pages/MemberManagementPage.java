package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.service.MembershipCardPrintService;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.members.MemberManagementView;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/member-management", layout = AdminLayout.class)
@PageTitle("Member Management")
@RolesAllowed(SecurityConstants.Roles.ADMIN)
public class MemberManagementPage extends MemberManagementView {
    public MemberManagementPage(
            MemberService memberService,
            MembershipService membershipService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler,
            MembershipCardPrintService printService
    ) {
        super(memberService, membershipService, printService, passwordProperties, uiExceptionHandler, uiNotificationHandler);
    }
}
