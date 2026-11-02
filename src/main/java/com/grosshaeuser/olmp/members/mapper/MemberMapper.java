package com.grosshaeuser.olmp.members.mapper;

import com.grosshaeuser.olmp.shared.MappingChecks;
import com.grosshaeuser.olmp.members.dto.*;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MemberDTO mapMemberToMemberDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getLastName(),
                member.getFirstName(),
                member.getDateOfBirth(),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(member.getCreatedAt()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(member.getUpdatedAt())
        );
    }

    public Member createMemberFromMemberRegistrationData(MemberRegistrationDTO dto) {
        return Member.builder()
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
    }

    public MemberAccountCreateDTO createAccountDtoFromMemberRegistrationData(MemberRegistrationDTO dto, Member member) {
        return new MemberAccountCreateDTO(
                member,
                dto.getEmail(),
                dto.getPassword(),
                dto.isVerified(),
                dto.isEnabled()
        );
    }

    public MemberContactInformationCreateDTO createContactDetailsDtoFromMemberRegistrationData(MemberRegistrationDTO dto, Member member) {
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

    public MemberEditDTO mergeMemberInformationToEditDto(Member member, MemberAccount account, MemberContactInformation contactInformation) {
        return MemberEditDTO.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .birthDate(member.getDateOfBirth())
                .createdAt(MappingChecks.saveOffsetDateTimeToLocalDateTime(member.getCreatedAt()))
                .updatedAt(MappingChecks.saveOffsetDateTimeToLocalDateTime(member.getUpdatedAt()))

                .account(mapMemberAccountToEditDto(account))
                .contact(mapMemberContactInformationToEditDto(contactInformation))
                .build();
    }

    public MemberAccountEditDTO mapMemberAccountToEditDto(MemberAccount account) {
        if (account != null) {
            return MemberAccountEditDTO.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .verified(account.isVerified())
                    .enabled(account.isEnabled())
                    .failedLoginAttempts(account.getFailedLoginAttempts())
                    .lockoutUntil(MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getLockoutUntil()))
                    .locked(account.isLocked())
                    .accountCreatedAt(MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getCreatedAt()))
                    .accountUpdatedAt(MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getUpdatedAt()))
                    .accountLastAccessedAt(MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getLastAccess()))
                    .build();
        }
        return new MemberAccountEditDTO();
    }

    public MemberContactInformationEditDTO mapMemberContactInformationToEditDto(MemberContactInformation memberContactInformation) {
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

    public MemberContactInformationCreateDTO createDtoForNewContactInformationInUpdateProcess(Member member, MemberContactInformationEditDTO dto) {
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
