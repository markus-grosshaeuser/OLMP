package com.grosshaeuser.olmp.membership.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MembershipServiceIntegrationTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void createMembershipPersistsMembershipWithGeneratedMembershipNumber() {
        Member member = memberRepository.save(member("Doe", "Jane"));

        MembershipCreateDTO dto = new MembershipCreateDTO();
        dto.setMember(member);
        dto.setMembershipType("PREMIUM");
        dto.setMembershipStatus("ACTIVE");
        dto.setMembershipStartDate(LocalDate.of(2026, 10, 1));
        dto.setMembershipExpirationDate(LocalDate.of(2027, 10, 1));
        dto.setMembershipRenewalDate(LocalDate.of(2027, 9, 1));
        dto.setMembershipCancellationDate(null);

        Optional<Membership> result = membershipService.createMembership(dto);

        assertThat(result).isPresent();

        Membership createdMembership = result.get();
        assertThat(createdMembership.getId()).isNotNull();
        assertThat(createdMembership.getMembershipNumber()).isNotBlank();
        assertThat(createdMembership.getMember().getId()).isEqualTo(member.getId());
        assertThat(createdMembership.getType()).isEqualTo(Membership.MembershipType.PREMIUM);
        assertThat(createdMembership.getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
        assertThat(createdMembership.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(createdMembership.getExpirationDate()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(createdMembership.getRenewalDate()).isEqualTo(LocalDate.of(2027, 9, 1));
        assertThat(createdMembership.getCancellationDate()).isNull();

        Optional<Membership> persistedMembership = membershipRepository.findById(createdMembership.getId());

        assertThat(persistedMembership).isPresent();
        assertThat(persistedMembership.get().getMembershipNumber()).isEqualTo(createdMembership.getMembershipNumber());
        assertThat(persistedMembership.get().getType()).isEqualTo(Membership.MembershipType.PREMIUM);
        assertThat(persistedMembership.get().getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
    }

    @Test
    void findByMemberIdReturnsPersistedMembership() {
        Member member = memberRepository.save(member("Doe", "John"));

        Membership membership = membershipRepository.save(Membership.builder()
                .member(member)
                .membershipNumber("MEMBERSHIP-FIND-001")
                .type(Membership.MembershipType.STANDARD)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .build());

        Optional<Membership> result = membershipService.findByMemberId(member.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(membership.getId());
        assertThat(result.get().getMember().getId()).isEqualTo(member.getId());
        assertThat(result.get().getMembershipNumber()).isEqualTo("MEMBERSHIP-FIND-001");
        assertThat(result.get().getType()).isEqualTo(Membership.MembershipType.STANDARD);
        assertThat(result.get().getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
    }

    @Test
    void findByMemberIdReturnsEmptyWhenMemberHasNoMembership() {
        Member member = memberRepository.save(member("Doe", "NoMembership"));

        Optional<Membership> result = membershipService.findByMemberId(member.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void updateMembershipUpdatesAndPersistsMembership() {
        Member member = memberRepository.save(member("Doe", "Update"));

        Membership membership = membershipRepository.save(Membership.builder()
                .member(member)
                .membershipNumber("MEMBERSHIP-UPDATE-001")
                .type(Membership.MembershipType.STANDARD)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .renewalDate(null)
                .cancellationDate(null)
                .build());

        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(membership.getId())
                .membershipType("STUDENT")
                .membershipStatus("SUSPENDED")
                .expirationDate("2027-04-01")
                .renewalDate("2027-03-01")
                .cancellationDate("2027-02-01")
                .build();

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).isPresent();

        Membership updatedMembership = result.get();
        assertThat(updatedMembership.getId()).isEqualTo(membership.getId());
        assertThat(updatedMembership.getMembershipNumber()).isEqualTo("MEMBERSHIP-UPDATE-001");
        assertThat(updatedMembership.getType()).isEqualTo(Membership.MembershipType.STUDENT);
        assertThat(updatedMembership.getStatus()).isEqualTo(Membership.MembershipStatus.SUSPENDED);
        assertThat(updatedMembership.getExpirationDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(updatedMembership.getRenewalDate()).isEqualTo(LocalDate.of(2027, 3, 1));
        assertThat(updatedMembership.getCancellationDate()).isEqualTo(LocalDate.of(2027, 2, 1));

        Optional<Membership> persistedMembership = membershipRepository.findById(membership.getId());

        assertThat(persistedMembership).isPresent();
        assertThat(persistedMembership.get().getType()).isEqualTo(Membership.MembershipType.STUDENT);
        assertThat(persistedMembership.get().getStatus()).isEqualTo(Membership.MembershipStatus.SUSPENDED);
        assertThat(persistedMembership.get().getExpirationDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(persistedMembership.get().getRenewalDate()).isEqualTo(LocalDate.of(2027, 3, 1));
        assertThat(persistedMembership.get().getCancellationDate()).isEqualTo(LocalDate.of(2027, 2, 1));
    }

    @Test
    void updateMembershipPersistsNullOptionalDatesWhenDtoContainsBlankDates() {
        Member member = memberRepository.save(member("Doe", "BlankDates"));

        Membership membership = membershipRepository.save(Membership.builder()
                .member(member)
                .membershipNumber("MEMBERSHIP-BLANK-DATES-001")
                .type(Membership.MembershipType.PREMIUM)
                .status(Membership.MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .renewalDate(LocalDate.of(2026, 12, 1))
                .cancellationDate(LocalDate.of(2026, 11, 1))
                .build());

        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(membership.getId())
                .membershipType("SENIOR")
                .membershipStatus("ACTIVE")
                .expirationDate("2027-06-01")
                .renewalDate("")
                .cancellationDate("")
                .build();

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(Membership.MembershipType.SENIOR);
        assertThat(result.get().getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
        assertThat(result.get().getExpirationDate()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(result.get().getRenewalDate()).isNull();
        assertThat(result.get().getCancellationDate()).isNull();

        Optional<Membership> persistedMembership = membershipRepository.findById(membership.getId());

        assertThat(persistedMembership).isPresent();
        assertThat(persistedMembership.get().getRenewalDate()).isNull();
        assertThat(persistedMembership.get().getCancellationDate()).isNull();
    }

    @Test
    void updateMembershipReturnsEmptyWhenMembershipDoesNotExist() {
        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(999_999L)
                .membershipType("STANDARD")
                .membershipStatus("ACTIVE")
                .expirationDate("2027-01-01")
                .renewalDate("")
                .cancellationDate("")
                .build();

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).isEmpty();
    }

    private static Member member(String lastName, String firstName) {
        return Member.builder()
                .lastName(lastName)
                .firstName(firstName)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }
}