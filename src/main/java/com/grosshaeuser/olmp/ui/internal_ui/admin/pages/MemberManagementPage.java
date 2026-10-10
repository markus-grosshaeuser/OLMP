package com.grosshaeuser.olmp.ui.internal_ui.admin.pages;

import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.ui.internal_ui.admin.AdminLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.MemberManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/member-management", layout = AdminLayout.class)
@PageTitle("Member Management")
@RolesAllowed("ADMIN")
public class MemberManagementPage extends MemberManagementView {
    public MemberManagementPage(MemberService memberService) {
        super(memberService);
    }
}
