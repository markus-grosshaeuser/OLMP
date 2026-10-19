package com.grosshaeuser.olmp.memberships.dto;

import lombok.*;

import java.time.LocalDate;

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
    private LocalDate startDate;
    private LocalDate expirationDate;
    private LocalDate renewalDate;
    private LocalDate cancellationDate;
}
