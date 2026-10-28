package com.grosshaeuser.olmp.members.dto;

import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberRegistrationDTO {
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;

    private String email;
    private String password;
    private boolean verified = true;
    private boolean enabled = true;

    private boolean withContactDetails = true;
    private String phoneNumber;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;

    private boolean withMembership = true;
    private String membershipType = MembershipType.STANDARD.name();
    private String membershipStatus = MembershipStatus.ACTIVE.name();
    private LocalDate membershipStartDate = LocalDate.now();
    private LocalDate membershipEndDate = LocalDate.now().plusYears(1);
    private LocalDate membershipRenewalDate;
    private LocalDate membershipCancellationDate;

}
