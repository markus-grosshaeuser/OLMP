package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.service.impl.MemberAccountServiceImpl;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberAccountServiceTest {

    @Mock
    private MemberAccountRepository memberAccountRepository;

    @Mock
    private PasswordProperties passwordProperties;

    @Mock
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private MemberAccountServiceImpl serviceUnderTest;



    @Test
    void findByMemberIdThrowsAccessDeniedExceptionIfUserLacksMemberAccountReadPermission() {
        givenUserLacksPermission(READ_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.findByMemberId(any()));
    }

    @Test
    void findByMemberExecutesIfUserHasMemberAccountReadPermission() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        serviceUnderTest.findByMemberId(999L);
        verify(memberAccountRepository, times(1)).findByMemberId(any());
    }





    @Test
    void createMemberAccountThrowsAccessDeniedExceptionIfUserLacksMemberAccountCreatePermission() {
        givenUserLacksPermission(CREATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.createMemberAccount(any()));
    }

    @Test
    void createMemberAccountThrowsIllegalArgumentExceptionIfPasswordRequirementsNotMet() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        when(passwordProperties.getRegExPattern()).thenReturn("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,}$");
        MemberAccountCreateDTO dto = new MemberAccountCreateDTO();
        dto.setEmail("email@example.com");
        dto.setPassword("password");
        assertThrows(IllegalArgumentException.class, () -> serviceUnderTest.createMemberAccount(dto));
    }

    @Test
    void createMemberThrowsDataIntegrityViolationExceptionIfEmailAlreadyExists() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        MemberAccountCreateDTO requested = new MemberAccountCreateDTO();
        requested.setEmail("email@example.com");
        requested.setPassword("Password1!");
        when(memberAccountRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.createMemberAccount(requested));
    }





    @Test
    void updateThrowsAccessDeniedExceptionIfUserLacksMemberAccountUpdatePermission() {
        givenUserLacksPermission(UPDATE_MEMBER_ACCOUNT);
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.update(new MemberAccountEditDTO()));
    }

    @Test
    void updateThrowsAccessDeniedExceptionIfUserAttemptsToVerifyMemberButLacksPermission() {
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserLacksPermission(VERIFY_MEMBER);
        MemberAccountEditDTO dto = MemberAccountEditDTO.builder()
                .verified(true)
                .build();
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.update(dto));
    }

    @Test
    void updateThrowsDataIntegrityViolationExceptionIfEmailWouldUpdateToAlreadyExistingEmail() {
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        MemberAccountEditDTO dto = MemberAccountEditDTO.builder()
                .email("email@example.com")
                .build();
        when(memberAccountRepository.findById(any())).thenReturn(Optional.ofNullable(MemberAccount.builder().email("test@example.com").build()));
        when(memberAccountRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.update(dto));
    }




    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }

    private void givenUserLacksPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(false);
    }
}