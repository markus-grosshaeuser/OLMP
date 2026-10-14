package com.grosshaeuser.olmp.memberships.mapper;

import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;

import java.time.format.DateTimeFormatter;

public class MembershipMapper {
    public static MembershipCreateDTO createMembershipCreateDtoFromMembershipRegistrationData(MemberRegistrationDTO dto, Member member) {
        return new MembershipCreateDTO(
                member,
                dto.getMembershipType(),
                dto.getMembershipStatus(),
                dto.getMembershipStartDate(),
                dto.getMembershipEndDate(),
                dto.getMembershipRenewalDate(),
                dto.getMembershipCancellationDate()
        );
    }

    public static Membership createMembershipFromMembershipCreateDto(MembershipCreateDTO dto, String membershipNumber) {
        return Membership.builder()
                .member(dto.getMember())
                .membershipNumber(membershipNumber)
                .type(Membership.MembershipType.valueOf(dto.getMembershipType()))
                .status(Membership.MembershipStatus.valueOf(dto.getMembershipStatus()))
                .startDate(dto.getMembershipStartDate())
                .expirationDate(dto.getMembershipExpirationDate())
                .renewalDate(dto.getMembershipRenewalDate())
                .cancellationDate(dto.getMembershipCancellationDate())
                .build();
    }

    public static MembershipEditDTO mapMembershipToEditDto(Membership membership) {
        if (membership != null && membership.getMember() != null) {
            return new MembershipEditDTO(
                    membership.getId(),
                    membership.getMember().getId(),
                    membership.getMembershipNumber(),
                    membership.getType().name(),
                    membership.getStatus().name(),
                    membership.getStartDate() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd").format(membership.getStartDate()) : null,
                    membership.getExpirationDate() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd").format(membership.getExpirationDate()) : null,
                    membership.getRenewalDate() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd").format(membership.getRenewalDate()) : null,
                    membership.getCancellationDate() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd").format(membership.getCancellationDate()) : null
            );
        }
        return new MembershipEditDTO();
    }
}
