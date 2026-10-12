package com.grosshaeuser.olmp.memberships.dto;

import com.grosshaeuser.olmp.members.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipCreateDto {
    private Member member;
    private String membershipType;
    private String membershipStatus;
    private LocalDate membershipStartDate;
    private LocalDate membershipExpirationDate;
    private LocalDate membershipRenewalDate;
    private LocalDate membershipCancellationDate;
}
