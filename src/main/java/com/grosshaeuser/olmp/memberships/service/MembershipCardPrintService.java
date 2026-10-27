package com.grosshaeuser.olmp.memberships.service;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;

public interface MembershipCardPrintService {
    String createPrintDocument(MemberEditDTO member, MembershipEditDTO membership);
}
