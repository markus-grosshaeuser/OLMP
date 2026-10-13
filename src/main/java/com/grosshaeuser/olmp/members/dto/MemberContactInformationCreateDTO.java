package com.grosshaeuser.olmp.members.dto;

import com.grosshaeuser.olmp.members.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberContactInformationCreateDTO {
    private Member member;
    private String phoneNumber;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
}
