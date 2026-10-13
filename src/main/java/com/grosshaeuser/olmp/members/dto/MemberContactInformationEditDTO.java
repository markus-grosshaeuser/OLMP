package com.grosshaeuser.olmp.members.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class MemberContactInformationEditDTO {
    private Long id;
    private Long memberId;
    private String phoneNumber;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
}
