package com.grosshaeuser.olmp.members.mapper;

import com.grosshaeuser.olmp.members.dto.*;
import com.grosshaeuser.olmp.members.entity.Member;

import java.time.format.DateTimeFormatter;

public class MemberMapper {
    public static MemberDTO mapMemberToMemberDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getLastName(),
                member.getFirstName(),
                member.getDateOfBirth() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd").format(member.getDateOfBirth()) : "",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(member.getCreatedAt()),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(member.getUpdatedAt())
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
}
