package com.grosshaeuser.olmp.memberships.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class MembershipEditDTO {
    private Long id;
    private Long memberId;
    private String membershipNumber;
    private String membershipType;
    private String membershipStatus;
    private String startDate;
    private String expirationDate;
    private String renewalDate;
    private String cancellationDate;
}
