package com.grosshaeuser.olmp.memberships.mapper;

import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDto;

public class MembershipMapper {
    public static MembershipCreateDto createMembershipCreateDtoFromMembershipRegistrationData(MemberRegistrationDTO dto, Member member) {
        return new MembershipCreateDto(
                member,
                dto.getMembershipType(),
                dto.getMembershipStatus(),
                dto.getMembershipStartDate(),
                dto.getMembershipEndDate(),
                dto.getMembershipRenewalDate(),
                dto.getMembershipCancellationDate()
        );
    }
}
