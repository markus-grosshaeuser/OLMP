package com.grosshaeuser.olmp.memberships.mapper;

import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.shared.AbstractMapper;

import java.time.LocalDate;

public class MembershipMapper extends AbstractMapper {
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
                .type(MembershipType.valueOf(dto.getMembershipType()))
                .status(MembershipStatus.valueOf(dto.getMembershipStatus()))
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
                    membership.getStartDate(),
                    membership.getExpirationDate(),
                    membership.getRenewalDate(),
                    membership.getCancellationDate()
            );
        }
        return new MembershipEditDTO();
    }

    public static MembershipCreateDTO createDtoForNewMembershipInUpdateProcess(Member member, MembershipEditDTO dto) {
        return new MembershipCreateDTO(
                member,
                dto.getMembershipType(),
                dto.getMembershipStatus(),
                dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now(),
                dto.getExpirationDate(),
                dto.getRenewalDate(),
                dto.getCancellationDate()
        );
    }

}
