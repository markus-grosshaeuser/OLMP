package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.impl.MemberServiceImpl;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private MemberServiceImpl serviceUnderTest;

    @BeforeEach
    void setUp() {

    }


    @Test
    void getMembersThrowsAccessDeniedExceptionIfUserLacksMemberAccountReadPermission() {
        givenUserLacksPermission(READ_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.getMembers(null, null));
        verify(memberRepository, times(0)).search("", Pageable.unpaged());
    }

    @Test
    void getMembersExecutesIfUserHasMemberAccountReadPermission() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        assertThrows(NullPointerException.class, () -> serviceUnderTest.getMembers(null, null));
        verify(memberRepository, times(1)).search(any(), any());
    }





    @Test
    void createMemberThrowsAccessDeniedExceptionIfUserLacksMemberAccountCreatePermission() {
        givenUserLacksPermission(CREATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.createMember(null));
        verify(memberRepository, times(0)).save(any());
    }

    @Test
    void createMemberExecutesIfUserHasMemberAccountCreatePermission() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        assertThrows(NullPointerException.class, () -> serviceUnderTest.createMember(new MemberRegistrationDTO()));
        verify(memberRepository, times(1)).save(any());
    }





    @Test
    void getDetailedMemberThrowsAccessDeniedExceptionIfUserLacksMemberAccountReadPermission() {
        givenUserLacksPermission(READ_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.getDetailedMember(null));
    }

    @Test
    void getDetailedMemberExecutesIfUserHasMemberAccountReadPermission() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        when(memberRepository.findById(any())).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), serviceUnderTest.getDetailedMember(999L));
        verify(memberRepository, times(1)).findById(any());
    }





    @Test
    void updateMemberThrowsAccessDeniedExceptionIfUserLacksMemberAccountUpdatePermission() {
        givenUserLacksPermission(UPDATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.updateMember(MemberEditDTO.builder().build()));
    }

    @Test
    void updateMemberExecutesIfUserHasMemberAccountUpdatePermission() {
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        when(memberRepository.findById(any())).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), serviceUnderTest.updateMember(MemberEditDTO.builder().build()));
        verify(memberRepository, times(1)).findById(any());
    }





    @Test
    void deleteMemberThrowsAccessDeniedExceptionIfUserLacksMemberAccountDeletePermission() {
        givenUserLacksPermission(DELETE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.deleteMember(999L));
    }

    @Test
    void deleteMemberSilentlyIgnoresIfTheProvidedMemberIdDoesNotExist() {
        givenUserHasPermission(DELETE_MEMBER_ACCOUNT);
        serviceUnderTest.deleteMember(999L);
        verify(memberRepository, times(1)).deleteById(999L);
    }





    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }

    private void givenUserLacksPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(false);
    }

}