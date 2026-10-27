package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;

@Getter
public class PrintMembershipCardRequestEvent extends ComponentEvent<Component> {

    private final MemberEditDTO member;
    private final MembershipEditDTO membership;

    public PrintMembershipCardRequestEvent(Component source, boolean fromClient, MemberEditDTO member, MembershipEditDTO membership) {
        super(source, fromClient);
        this.member = member;
        this.membership = membership;
    }
}
