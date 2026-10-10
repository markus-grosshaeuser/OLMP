package com.grosshaeuser.olmp.ui.internal_ui.employee.pages;

import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.ui.internal_ui.employee.EmployeeLayout;
import com.grosshaeuser.olmp.ui.internal_ui.shared.MemberManagementView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "staff/member-management", layout = EmployeeLayout.class)
@PageTitle("Member Management")
@RolesAllowed("STAFF")
public class MemberManagementPage extends MemberManagementView {
    public MemberManagementPage(MemberService memberService) {
        super(memberService);
    }
}
