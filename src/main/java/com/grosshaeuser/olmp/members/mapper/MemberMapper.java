package com.grosshaeuser.olmp.members.mapper;

import com.grosshaeuser.olmp.members.dto.*;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.shared.AbstractMapper;

public class MemberMapper extends AbstractMapper {
    public static MemberDTO mapMemberToMemberDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getLastName(),
                member.getFirstName(),
                member.getDateOfBirth(),
                saveOffsetDateTimeToLocalDateTime(member.getCreatedAt()),
                saveOffsetDateTimeToLocalDateTime(member.getUpdatedAt())
        );
    }

    public static Member createMemberFromMemberRegistrationData(MemberRegistrationDTO dto) {
        return Member.builder()
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
    }

    public static MemberAccountCreateDTO createAccountDtoFromMemberRegistrationData(MemberRegistrationDTO dto, Member member) {
        return new MemberAccountCreateDTO(
                member,
                dto.getEmail(),
                dto.getPassword(),
                dto.isVerified(),
                dto.isEnabled()
        );
    }

    public static MemberContactInformationCreateDTO createContactDetailsDtoFromMemberRegistrationData(MemberRegistrationDTO dto, Member member) {
        return new MemberContactInformationCreateDTO(
                member,
                dto.getPhoneNumber(),
                dto.getStreet(),
                dto.getHouseNumber(),
                dto.getPostalCode(),
                dto.getCity(),
                dto.getCountry()
        );
    }

    public static MemberEditDTO mergeMemberInformationToEditDto(Member member, MemberAccount account, MemberContactInformation contactInformation) {
        return MemberEditDTO.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .birthDate(member.getDateOfBirth())
                .createdAt(saveOffsetDateTimeToLocalDateTime(member.getCreatedAt()))
                .updatedAt(saveOffsetDateTimeToLocalDateTime(member.getUpdatedAt()))

                .account(mapMemberAccountToEditDto(account))
                .contact(mapMemberContactInformationToEditDto(contactInformation))
                .build();
    }

    public static MemberAccountEditDTO mapMemberAccountToEditDto(MemberAccount account) {
        if (account != null) {
            return MemberAccountEditDTO.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .verified(account.isVerified())
                    .enabled(account.isEnabled())
                    .failedLoginAttempts(account.getFailedLoginAttempts())
                    .lockoutUntil(saveOffsetDateTimeToLocalDateTime(account.getLockoutUntil()))
                    .locked(account.isLocked())
                    .accountCreatedAt(saveOffsetDateTimeToLocalDateTime(account.getCreatedAt()))
                    .accountUpdatedAt(saveOffsetDateTimeToLocalDateTime(account.getUpdatedAt()))
                    .accountLastAccessedAt(saveOffsetDateTimeToLocalDateTime(account.getLastAccess()))
                    .build();
        }
        return new MemberAccountEditDTO();
    }

    public static MemberContactInformationEditDTO mapMemberContactInformationToEditDto(MemberContactInformation memberContactInformation) {
        if (memberContactInformation != null && memberContactInformation.getMember() != null){
            return MemberContactInformationEditDTO.builder()
                    .id(memberContactInformation.getId())
                    .memberId(memberContactInformation.getMember().getId())
                    .phoneNumber(memberContactInformation.getPhoneNumber())
                    .street(memberContactInformation.getStreet())
                    .houseNumber(memberContactInformation.getHouseNumber())
                    .postalCode(memberContactInformation.getPostalCode())
                    .city(memberContactInformation.getCity())
                    .country(memberContactInformation.getCountry())
                    .build();
        }
        return new MemberContactInformationEditDTO();
    }

    public static MemberContactInformationCreateDTO createDtoForNewContactInformationInUpdateProcess(Member member, MemberContactInformationEditDTO dto) {
        return new MemberContactInformationCreateDTO(
                member,
                dto.getPhoneNumber(),
                dto.getStreet(),
                dto.getHouseNumber(),
                dto.getPostalCode(),
                dto.getCity(),
                dto.getCountry()
        );
    }


}
