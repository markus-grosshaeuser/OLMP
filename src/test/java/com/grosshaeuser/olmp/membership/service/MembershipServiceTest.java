package com.grosshaeuser.olmp.membership.service;

import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import com.grosshaeuser.olmp.memberships.service.MembershipNumberGenerator;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.memberships.service.impl.MembershipServiceImpl;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private MembershipRepository membershipRepository;
    private MembershipNumberGenerator membershipNumberGenerator;
    private MembershipService membershipService;
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        membershipNumberGenerator = mock(MembershipNumberGenerator.class);
        authenticationContext = mock(AuthenticationContext.class);


        membershipService = new MembershipServiceImpl(
                membershipRepository,
                membershipNumberGenerator,
                authenticationContext
        );
    }

    @Test
    void createMembershipGeneratesMembershipNumberPersistsMembershipAndReturnsSavedEntity() {
        givenUserHasPermission(CREATE_MEMBERSHIP);

        Member member = mock(Member.class);

        MembershipCreateDTO dto = new MembershipCreateDTO();
        dto.setMember(member);
        dto.setMembershipType("PREMIUM");
        dto.setMembershipStatus("ACTIVE");
        dto.setMembershipStartDate(LocalDate.of(2026, 10, 1));
        dto.setMembershipExpirationDate(LocalDate.of(2027, 10, 1));
        dto.setMembershipRenewalDate(LocalDate.of(2027, 9, 1));
        dto.setMembershipCancellationDate(null);

        Membership savedMembership = Membership.builder()
                .id(1L)
                .member(member)
                .membershipNumber("MBR-TEST-001")
                .type(MembershipType.PREMIUM)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 10, 1))
                .expirationDate(LocalDate.of(2027, 10, 1))
                .renewalDate(LocalDate.of(2027, 9, 1))
                .cancellationDate(null)
                .build();

        when(membershipNumberGenerator.generate()).thenReturn("MBR-TEST-001");
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);

        Optional<Membership> result = membershipService.createMembership(dto);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(savedMembership);

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());

        Membership membershipToSave = membershipCaptor.getValue();
        assertThat(membershipToSave.getMember()).isSameAs(member);
        assertThat(membershipToSave.getMembershipNumber()).isEqualTo("MBR-TEST-001");
        assertThat(membershipToSave.getType()).isEqualTo(MembershipType.PREMIUM);
        assertThat(membershipToSave.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membershipToSave.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(membershipToSave.getExpirationDate()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(membershipToSave.getRenewalDate()).isEqualTo(LocalDate.of(2027, 9, 1));
        assertThat(membershipToSave.getCancellationDate()).isNull();

        verify(membershipNumberGenerator).generate();
    }

    @Test
    void getMembershipPeriodsInMonthsReturnsConfiguredPeriods() {
        HashMap<String, Integer> result = membershipService.getMembershipPeriodsInMonths();

        assertThat(result)
                .containsEntry("STANDARD", 12)
                .containsEntry("STUDENT", 6)
                .containsEntry("SENIOR", 12)
                .containsEntry("PREMIUM", 12)
                .hasSize(4);

        verifyNoInteractions(membershipRepository, membershipNumberGenerator);
    }

    @Test
    void findByMemberIdDelegatesToRepositoryAndReturnsMembership() {
        givenUserHasPermission(READ_MEMBERSHIP);

        Membership membership = Membership.builder()
                .id(1L)
                .membershipNumber("MBR-TEST-001")
                .type(MembershipType.STANDARD)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 10, 1))
                .build();

        when(membershipRepository.findActiveByMemberId(10L)).thenReturn(Optional.of(membership));

        Optional<Membership> result = membershipService.findActiveByMemberId(10L);

        assertThat(result).containsSame(membership);

        verify(membershipRepository).findActiveByMemberId(10L);
        verifyNoInteractions(membershipNumberGenerator);
    }

    @Test
    void findByMemberIdReturnsEmptyWhenRepositoryReturnsEmpty() {
        givenUserHasPermission(READ_MEMBERSHIP);

        when(membershipRepository.findActiveByMemberId(99L)).thenReturn(Optional.empty());

        Optional<Membership> result = membershipService.findActiveByMemberId(99L);

        assertThat(result).isEmpty();

        verify(membershipRepository).findActiveByMemberId(99L);
        verifyNoInteractions(membershipNumberGenerator);
    }

    @Test
    void updateMembershipUpdatesExistingMembershipPersistsItAndReturnsSavedMembership() {
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        Membership existingMembership = Membership.builder()
                .id(1L)
                .membershipNumber("MBR-TEST-001")
                .type(MembershipType.STANDARD)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .renewalDate(null)
                .cancellationDate(null)
                .build();

        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(1L)
                .membershipType("STUDENT")
                .membershipStatus("SUSPENDED")
                .expirationDate(LocalDate.of(2027,4,1))
                .renewalDate(LocalDate.of(2027,3,1))
                .cancellationDate(LocalDate.of(2027,2,1))
                .build();

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(existingMembership));
        when(membershipRepository.save(existingMembership)).thenReturn(existingMembership);

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).containsSame(existingMembership);

        assertThat(existingMembership.getType()).isEqualTo(MembershipType.STUDENT);
        assertThat(existingMembership.getStatus()).isEqualTo(MembershipStatus.SUSPENDED);
        assertThat(existingMembership.getExpirationDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(existingMembership.getRenewalDate()).isEqualTo(LocalDate.of(2027, 3, 1));
        assertThat(existingMembership.getCancellationDate()).isEqualTo(LocalDate.of(2027, 2, 1));

        verify(membershipRepository).findById(1L);
        verify(membershipRepository).save(existingMembership);
        verifyNoInteractions(membershipNumberGenerator);
    }

    @Test
    void updateMembershipSetsOptionalDatesToNullWhenBlank() {
        givenUserHasPermission(UPDATE_MEMBERSHIP);

        Membership existingMembership = Membership.builder()
                .id(1L)
                .membershipNumber("MBR-TEST-001")
                .type(MembershipType.PREMIUM)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2027, 1, 1))
                .renewalDate(LocalDate.of(2026, 12, 1))
                .cancellationDate(LocalDate.of(2026, 11, 1))
                .build();

        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(1L)
                .membershipType("SENIOR")
                .membershipStatus("ACTIVE")
                .expirationDate(LocalDate.of(2027, 6, 1))
                .renewalDate(null)
                .cancellationDate(null)
                .build();

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(existingMembership));
        when(membershipRepository.save(existingMembership)).thenReturn(existingMembership);

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).containsSame(existingMembership);

        assertThat(existingMembership.getType()).isEqualTo(MembershipType.SENIOR);
        assertThat(existingMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(existingMembership.getExpirationDate()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(existingMembership.getRenewalDate()).isNull();
        assertThat(existingMembership.getCancellationDate()).isNull();

        verify(membershipRepository).findById(1L);
        verify(membershipRepository).save(existingMembership);
    }

    @Test
    void updateMembershipReturnsEmptyWhenMembershipDoesNotExist() {
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        MembershipEditDTO dto = MembershipEditDTO.builder()
                .id(99L)
                .membershipType("STANDARD")
                .membershipStatus("ACTIVE")
                .expirationDate(LocalDate.of(2027, 1, 1))
                .renewalDate(null)
                .cancellationDate(null)
                .build();

        when(membershipRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Membership> result = membershipService.updateMembership(dto);

        assertThat(result).isEmpty();

        verify(membershipRepository).findById(99L);
        verify(membershipRepository, never()).save(any(Membership.class));
        verifyNoInteractions(membershipNumberGenerator);
    }

    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }

    @Test
    void findAllByMemberIdRequiresReadPermission() {
        assertThatThrownBy(() -> membershipService.findAllByMemberId(10L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        verifyNoInteractions(membershipRepository);
    }

    @Test
    void findAllByMemberIdIncludesInactiveMembershipsInRepositoryOrder() {
        givenUserHasPermission(READ_MEMBERSHIP);
        Membership expired = Membership.builder().status(MembershipStatus.EXPIRED).build();
        Membership cancelled = Membership.builder().status(MembershipStatus.CANCELLED).build();
        when(membershipRepository.findAllByMemberIdOrderByStartDateDescIdDesc(10L)).thenReturn(List.of(expired, cancelled));
        assertThat(membershipService.findAllByMemberId(10L)).containsExactly(expired, cancelled);
    }
}
