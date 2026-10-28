package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.service.MembershipCardPrintService;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class MemberContext {
    private final MemberService memberService;
    private final MembershipService membershipService;
    private final MembershipCardPrintService printService;
    private final PasswordProperties passwordProperties;

    public String createPrintDocument(MemberEditDTO member, MembershipEditDTO membership) {
        return printService.createPrintDocument(member, membership);
    }

    public String getRegExPattern() {
        return passwordProperties.getRegExPattern();
    }

    public String getPasswordRequirementDescription() {
        return passwordProperties.getPasswordRequirementDescription();
    }
}
