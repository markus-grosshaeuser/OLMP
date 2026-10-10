package com.grosshaeuser.olmp.members.mapper;

import com.grosshaeuser.olmp.members.dto.MemberDTO;
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
}
