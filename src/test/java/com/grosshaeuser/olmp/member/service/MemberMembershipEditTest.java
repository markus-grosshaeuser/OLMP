package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import com.grosshaeuser.olmp.members.service.MemberContactInformationService;
import com.grosshaeuser.olmp.members.service.impl.MemberServiceImpl;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.mapper.MembershipMapper;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberMembershipEditTest {
    @Mock private MemberRepository members;
    @Mock private MemberAccountService accounts;
    @Mock private MemberContactInformationService contacts;
    @Mock private MembershipService memberships;
    @Mock private AuthenticationContext authentication;
    private MemberServiceImpl service;
    private final Member member = Member.builder().id(10L).firstName("Ada").lastName("Lovelace").build();

    @BeforeEach
    void setUp() {
        service = new MemberServiceImpl(members, accounts, contacts, memberships, authentication);
        when(members.findById(member.getId())).thenReturn(Optional.of(member));
    }

    @Test
    void loadsHistoryEvenWhenThereIsNoActiveMembership() {
        when(authentication.hasAuthority(READ_MEMBER_ACCOUNT)).thenReturn(true);
        var expired = membership(1L, MembershipStatus.EXPIRED);
        var cancelled = membership(2L, MembershipStatus.CANCELLED);
        when(memberships.findAllByMemberId(member.getId())).thenReturn(List.of(expired, cancelled));

        assertThat(service.getDetailedMember(member.getId()).orElseThrow().getMemberships())
                .extracting(MembershipEditDTO::getMembershipStatus).containsExactly("EXPIRED", "CANCELLED");
        verify(memberships, never()).findActiveByMemberId(any());
    }

    @Test
    void updatesHistoricalRowCreatesNewRowAndLeavesUnchangedCancelledRowAlone() {
        givenUpdate();
        var expired = membership(1L, MembershipStatus.EXPIRED);
        var cancelled = membership(2L, MembershipStatus.CANCELLED);
        var updated = MembershipMapper.mapMembershipToEditDto(expired).toBuilder().membershipType("STUDENT").build();
        var added = MembershipEditDTO.builder().membershipType("PREMIUM").membershipStatus("ACTIVE")
                .startDate(LocalDate.of(2026, 10, 1)).build();
        when(memberships.findAllByMemberId(member.getId())).thenReturn(List.of(expired, cancelled));
        when(memberships.updateMembership(updated)).thenReturn(Optional.of(expired));
        when(memberships.createMembership(any())).thenReturn(Optional.of(membership(3L, MembershipStatus.ACTIVE)));

        service.updateMember(edit(List.of(updated, MembershipMapper.mapMembershipToEditDto(cancelled), added))).orElseThrow();

        verify(memberships).updateMembership(updated);
        verify(memberships, times(1)).updateMembership(any());
        var create = ArgumentCaptor.forClass(MembershipCreateDTO.class);
        verify(memberships).createMembership(create.capture());
        assertThat(create.getValue().getMember()).isSameAs(member);
        assertThat(create.getValue().getMembershipType()).isEqualTo("PREMIUM");
        assertThat(create.getValue().getMembershipStartDate()).isEqualTo(added.getStartDate());
    }

    @Test
    void rejectsForeignMembershipIdBeforeWritingAnyMemberships() {
        givenUpdate();
        when(memberships.findAllByMemberId(member.getId())).thenReturn(List.of());
        var foreign = MembershipMapper.mapMembershipToEditDto(membership(99L, MembershipStatus.ACTIVE));
        var added = MembershipEditDTO.builder().membershipType("STANDARD").membershipStatus("ACTIVE").build();

        assertThatThrownBy(() -> service.updateMember(edit(List.of(added, foreign))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(memberships, never()).createMembership(any());
        verify(memberships, never()).updateMembership(any());
    }

    @Test
    void rejectsDuplicateMembershipIds() {
        givenUpdate();
        var existing = membership(1L, MembershipStatus.ACTIVE);
        when(memberships.findAllByMemberId(member.getId())).thenReturn(List.of(existing));
        var row = MembershipMapper.mapMembershipToEditDto(existing);

        assertThatThrownBy(() -> service.updateMember(edit(List.of(row, row))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(memberships, never()).updateMembership(any());
    }

    @Test
    void preservesMembershipsWhenOnlyProfileWasChanged() {
        givenUpdate();
        var existing = membership(1L, MembershipStatus.EXPIRED);
        when(memberships.findAllByMemberId(member.getId())).thenReturn(List.of(existing));
        var dto = edit(List.of());
        dto.setWithMembershipDetails(false);

        assertThat(service.updateMember(dto).orElseThrow().getMemberships())
                .containsExactly(MembershipMapper.mapMembershipToEditDto(existing));
        verify(memberships, never()).createMembership(any());
        verify(memberships, never()).updateMembership(any());
    }

    private void givenUpdate() {
        when(authentication.hasAuthority(UPDATE_MEMBER_ACCOUNT)).thenReturn(true);
        when(members.save(member)).thenReturn(member);
    }

    private MemberEditDTO edit(List<MembershipEditDTO> rows) {
        return MemberEditDTO.builder().id(member.getId()).firstName("Ada").lastName("Lovelace")
                .withMembershipDetails(true).memberships(rows).build();
    }

    private Membership membership(Long id, MembershipStatus status) {
        return Membership.builder().id(id).member(member).membershipNumber("MBR-" + id)
                .type(MembershipType.STANDARD).status(status)
                .startDate(LocalDate.of(2025, 1, 1)).expirationDate(LocalDate.of(2026, 1, 1)).build();
    }
}
