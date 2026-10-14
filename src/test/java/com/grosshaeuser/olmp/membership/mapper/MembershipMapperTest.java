package com.grosshaeuser.olmp.membership.mapper;

import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.mapper.MembershipMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipMapperTest {

    @Test
    void createMembershipCreateDtoFromMembershipRegistrationDataMapsMembershipFields() {
        Member member = member(1L, "Doe", "Jane");

        MemberRegistrationDTO registrationDto = new MemberRegistrationDTO();
        registrationDto.setMembershipType("PREMIUM");
        registrationDto.setMembershipStatus("ACTIVE");
        registrationDto.setMembershipStartDate(LocalDate.of(2026, 10, 1));
        registrationDto.setMembershipEndDate(LocalDate.of(2027, 10, 1));
        registrationDto.setMembershipRenewalDate(LocalDate.of(2027, 9, 1));
        registrationDto.setMembershipCancellationDate(null);

        MembershipCreateDTO result = MembershipMapper.createMembershipCreateDtoFromMembershipRegistrationData(
                registrationDto,
                member
        );

        assertThat(result.getMember()).isSameAs(member);
        assertThat(result.getMembershipType()).isEqualTo("PREMIUM");
        assertThat(result.getMembershipStatus()).isEqualTo("ACTIVE");
        assertThat(result.getMembershipStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(result.getMembershipExpirationDate()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(result.getMembershipRenewalDate()).isEqualTo(LocalDate.of(2027, 9, 1));
        assertThat(result.getMembershipCancellationDate()).isNull();
    }

    @Test
    void createMembershipFromMembershipCreateDtoMapsDtoToMembershipEntity() {
        Member member = member(1L, "Doe", "Jane");

        MembershipCreateDTO dto = new MembershipCreateDTO();
        dto.setMember(member);
        dto.setMembershipType("STUDENT");
        dto.setMembershipStatus("SUSPENDED");
        dto.setMembershipStartDate(LocalDate.of(2026, 1, 1));
        dto.setMembershipExpirationDate(LocalDate.of(2026, 7, 1));
        dto.setMembershipRenewalDate(LocalDate.of(2026, 6, 1));
        dto.setMembershipCancellationDate(LocalDate.of(2026, 5, 1));

        Membership result = MembershipMapper.createMembershipFromMembershipCreateDto(
                dto,
                "MEMBERSHIP-001"
        );

        assertThat(result.getId()).isNull();
        assertThat(result.getMember()).isSameAs(member);
        assertThat(result.getMembershipNumber()).isEqualTo("MEMBERSHIP-001");
        assertThat(result.getType()).isEqualTo(Membership.MembershipType.STUDENT);
        assertThat(result.getStatus()).isEqualTo(Membership.MembershipStatus.SUSPENDED);
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(result.getRenewalDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getCancellationDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    void mapMembershipToEditDtoMapsMembershipToDtoAndFormatsDates() {
        Member member = member(10L, "Doe", "Jane");

        Membership membership = Membership.builder()
                .id(100L)
                .member(member)
                .membershipNumber("MEMBERSHIP-EDIT-001")
                .type(Membership.MembershipType.SENIOR)
                .status(Membership.MembershipStatus.CANCELLED)
                .startDate(LocalDate.of(2026, 2, 1))
                .expirationDate(LocalDate.of(2027, 2, 1))
                .renewalDate(LocalDate.of(2027, 1, 1))
                .cancellationDate(LocalDate.of(2026, 12, 1))
                .build();

        MembershipEditDTO result = MembershipMapper.mapMembershipToEditDto(membership);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getMemberId()).isEqualTo(10L);
        assertThat(result.getMembershipNumber()).isEqualTo("MEMBERSHIP-EDIT-001");
        assertThat(result.getMembershipType()).isEqualTo("SENIOR");
        assertThat(result.getMembershipStatus()).isEqualTo("CANCELLED");
        assertThat(result.getStartDate()).isEqualTo("2026-02-01");
        assertThat(result.getExpirationDate()).isEqualTo("2027-02-01");
        assertThat(result.getRenewalDate()).isEqualTo("2027-01-01");
        assertThat(result.getCancellationDate()).isEqualTo("2026-12-01");
    }

    @Test
    void mapMembershipToEditDtoKeepsDateFieldsNullWhenMembershipDatesAreNull() {
        Member member = member(10L, "Doe", "Jane");

        Membership membership = Membership.builder()
                .id(100L)
                .member(member)
                .membershipNumber("MEMBERSHIP-NULL-DATES-001")
                .type(Membership.MembershipType.STANDARD)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(null)
                .expirationDate(null)
                .renewalDate(null)
                .cancellationDate(null)
                .build();

        MembershipEditDTO result = MembershipMapper.mapMembershipToEditDto(membership);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getMemberId()).isEqualTo(10L);
        assertThat(result.getMembershipNumber()).isEqualTo("MEMBERSHIP-NULL-DATES-001");
        assertThat(result.getMembershipType()).isEqualTo("STANDARD");
        assertThat(result.getMembershipStatus()).isEqualTo("ACTIVE");
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getExpirationDate()).isNull();
        assertThat(result.getRenewalDate()).isNull();
        assertThat(result.getCancellationDate()).isNull();
    }

    @Test
    void mapMembershipToEditDtoReturnsEmptyDtoWhenMembershipIsNull() {
        MembershipEditDTO result = MembershipMapper.mapMembershipToEditDto(null);

        assertThat(result.getId()).isNull();
        assertThat(result.getMemberId()).isNull();
        assertThat(result.getMembershipNumber()).isNull();
        assertThat(result.getMembershipType()).isNull();
        assertThat(result.getMembershipStatus()).isNull();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getExpirationDate()).isNull();
        assertThat(result.getRenewalDate()).isNull();
        assertThat(result.getCancellationDate()).isNull();
    }

    @Test
    void mapMembershipToEditDtoReturnsEmptyDtoWhenMembershipHasNoMember() {
        Membership membership = Membership.builder()
                .id(100L)
                .member(null)
                .membershipNumber("MEMBERSHIP-NO-MEMBER-001")
                .type(Membership.MembershipType.STANDARD)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .build();

        MembershipEditDTO result = MembershipMapper.mapMembershipToEditDto(membership);

        assertThat(result.getId()).isNull();
        assertThat(result.getMemberId()).isNull();
        assertThat(result.getMembershipNumber()).isNull();
        assertThat(result.getMembershipType()).isNull();
        assertThat(result.getMembershipStatus()).isNull();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getExpirationDate()).isNull();
        assertThat(result.getRenewalDate()).isNull();
        assertThat(result.getCancellationDate()).isNull();
    }

    private static Member member(Long id, String lastName, String firstName) {
        return Member.builder()
                .id(id)
                .lastName(lastName)
                .firstName(firstName)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }
}