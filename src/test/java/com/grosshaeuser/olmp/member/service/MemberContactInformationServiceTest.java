package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.service.impl.MemberContactInformationServiceImpl;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberContactInformationServiceTest {

    @Mock
    private MemberContactInformationRepository memberContactInformationRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private MemberContactInformationServiceImpl serviceUnderTest;



    @Test
    void findByMemberIdThrowsAccessDeniedExceptionIfUserLacksMemberAccountReadPermission() {
        givenUserLacksPermission(READ_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.findByMemberId(1L));
    }

    @Test
    void findByMemberExecutesIfUserHasMemberAccountReadPermission() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        serviceUnderTest.findByMemberId(1L);
        verify(memberContactInformationRepository, times(1)).findByMemberId(any());
    }





    @Test
    void createThrowsAccessDeniedExceptionIfUserLacksMemberAccountCreatePermission() {
        givenUserLacksPermission(CREATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.create(new MemberContactInformationCreateDTO()));
    }

    @Test
    void createExecutesIfUserHasMemberAccountCreatePermission() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        assertThrows(NullPointerException.class, () -> serviceUnderTest.create(new MemberContactInformationCreateDTO()));
    }





    @Test
    void updateContactInformationThrowsAccessDeniedExceptionIfUserLacksMemberAccountUpdatePermission() {
        givenUserLacksPermission(UPDATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.updateContactInformation(new MemberContactInformationEditDTO()));
    }

    @Test
    void updateContactInformationExecutesIfUserHasMemberAccountUpdatePermission() {
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        serviceUnderTest.updateContactInformation(new MemberContactInformationEditDTO());
        verify(memberContactInformationRepository, times(1)).findById(any());
    }




    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }

    private void givenUserLacksPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(false);
    }
}