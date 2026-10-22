package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OlmpSecurityEventListenerTest {

    private static final String USERNAME = "security-test@example.com";

    @Mock
    private EmployeeAccountRepository employeeAccountRepository;
    @Mock
    private MemberAccountRepository memberAccountRepository;
    @InjectMocks
    private OlmpSecurityEventListener listener;

    @Test
    void employeeSuccessResetsLockoutAndRecordsLastAccess() {
        EmployeeAccount account = new EmployeeAccount();
        account.setFailedLoginAttempts(7);
        account.setLocked(true);
        account.setLockoutUntil(OffsetDateTime.now().plusMinutes(10));
        account.setLastAccess(OffsetDateTime.now().minusDays(1));
        EmployeePrincipal principal = mock(EmployeePrincipal.class);
        when(principal.getAccountId()).thenReturn(1L);
        when(employeeAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        OffsetDateTime before = OffsetDateTime.now();

        listener.onSuccess(success(principal));

        assertThat(account.getFailedLoginAttempts()).isZero();
        assertThat(account.isLocked()).isFalse();
        assertThat(account.getLockoutUntil()).isNull();
        assertThat(account.getLastAccess()).isBetween(before, OffsetDateTime.now());
        verify(employeeAccountRepository).save(account);
        verifyNoInteractions(memberAccountRepository);
    }

    @Test
    void memberSuccessResetsLockoutAndRecordsLastAccess() {
        MemberAccount account = new MemberAccount();
        account.setFailedLoginAttempts(7);
        account.setLocked(true);
        account.setLockoutUntil(OffsetDateTime.now().plusMinutes(10));
        account.setLastAccess(OffsetDateTime.now().minusDays(1));
        MemberPrincipal principal = mock(MemberPrincipal.class);
        when(principal.getAccountId()).thenReturn(2L);
        when(memberAccountRepository.findById(2L)).thenReturn(Optional.of(account));
        OffsetDateTime before = OffsetDateTime.now();

        listener.onSuccess(success(principal));

        assertThat(account.getFailedLoginAttempts()).isZero();
        assertThat(account.isLocked()).isFalse();
        assertThat(account.getLockoutUntil()).isNull();
        assertThat(account.getLastAccess()).isBetween(before, OffsetDateTime.now());
        verify(memberAccountRepository).save(account);
        verifyNoInteractions(employeeAccountRepository);
    }

    @Test
    void successForDeletedEmployeeDoesNotSaveAnAccount() {
        EmployeePrincipal principal = mock(EmployeePrincipal.class);
        when(principal.getAccountId()).thenReturn(1L);
        when(employeeAccountRepository.findById(1L)).thenReturn(Optional.empty());

        listener.onSuccess(success(principal));

        verify(employeeAccountRepository).findById(1L);
        verifyNoMoreInteractions(employeeAccountRepository);
        verifyNoInteractions(memberAccountRepository);
    }

    @Test
    void successForDeletedMemberDoesNotSaveAnAccount() {
        MemberPrincipal principal = mock(MemberPrincipal.class);
        when(principal.getAccountId()).thenReturn(2L);
        when(memberAccountRepository.findById(2L)).thenReturn(Optional.empty());

        listener.onSuccess(success(principal));

        verify(memberAccountRepository).findById(2L);
        verifyNoMoreInteractions(memberAccountRepository);
        verifyNoInteractions(employeeAccountRepository);
    }

    @Test
    void successWithUnrecognizedPrincipalDoesNotAccessRepositories() {
        listener.onSuccess(success("another-principal"));

        verifyNoInteractions(employeeAccountRepository, memberAccountRepository);
    }

    @ParameterizedTest
    @CsvSource({"0, false", "3, false", "4, true", "5, true"})
    void employeeBadCredentialsIncrementAttemptsAndLockAtThreshold(int previousAttempts, boolean locks) {
        EmployeeAccount account = new EmployeeAccount();
        account.setFailedLoginAttempts(previousAttempts);
        OffsetDateTime lastAccess = OffsetDateTime.now().minusDays(1);
        account.setLastAccess(lastAccess);
        when(employeeAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(account));
        OffsetDateTime before = OffsetDateTime.now();

        listener.onBadCredentials(badCredentials());

        assertThat(account.getFailedLoginAttempts()).isEqualTo(previousAttempts + 1);
        assertThat(account.isLocked()).isEqualTo(locks);
        if (locks) {
            assertThat(account.getLockoutUntil()).isBetween(before.plusMinutes(15), OffsetDateTime.now().plusMinutes(15));
        } else {
            assertThat(account.getLockoutUntil()).isNull();
        }
        assertThat(account.getLastAccess()).isEqualTo(lastAccess);
        verify(employeeAccountRepository).save(account);
        verify(memberAccountRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({"0, false", "3, false", "4, true", "5, true"})
    void memberBadCredentialsIncrementAttemptsAndLockAtThreshold(int previousAttempts, boolean locks) {
        MemberAccount account = new MemberAccount();
        account.setFailedLoginAttempts(previousAttempts);
        OffsetDateTime lastAccess = OffsetDateTime.now().minusDays(1);
        account.setLastAccess(lastAccess);
        when(memberAccountRepository.findByEmail(USERNAME)).thenReturn(Optional.of(account));
        OffsetDateTime before = OffsetDateTime.now();

        listener.onBadCredentials(badCredentials());

        assertThat(account.getFailedLoginAttempts()).isEqualTo(previousAttempts + 1);
        assertThat(account.isLocked()).isEqualTo(locks);
        if (locks) {
            assertThat(account.getLockoutUntil()).isBetween(before.plusMinutes(15), OffsetDateTime.now().plusMinutes(15));
        } else {
            assertThat(account.getLockoutUntil()).isNull();
        }
        assertThat(account.getLastAccess()).isEqualTo(lastAccess);
        verify(memberAccountRepository).save(account);
        verify(employeeAccountRepository, never()).save(any());
    }

    @Test
    void badCredentialsForUnknownUsernameDoesNotSaveAccounts() {
        listener.onBadCredentials(badCredentials());

        verify(employeeAccountRepository).findByUsername(USERNAME);
        verify(memberAccountRepository).findByEmail(USERNAME);
        verifyNoMoreInteractions(employeeAccountRepository, memberAccountRepository);
    }

    @Test
    void badCredentialsUpdateBothAccountsWhenIdentifiersMatch() {
        EmployeeAccount employee = new EmployeeAccount();
        MemberAccount member = new MemberAccount();
        when(employeeAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(employee));
        when(memberAccountRepository.findByEmail(USERNAME)).thenReturn(Optional.of(member));

        listener.onBadCredentials(badCredentials());

        assertThat(employee.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(member.getFailedLoginAttempts()).isEqualTo(1);
        verify(employeeAccountRepository).save(employee);
        verify(memberAccountRepository).save(member);
    }

    private AuthenticationSuccessEvent success(Object principal) {
        return new AuthenticationSuccessEvent(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
    }

    private AuthenticationFailureBadCredentialsEvent badCredentials() {
        return new AuthenticationFailureBadCredentialsEvent(
                UsernamePasswordAuthenticationToken.unauthenticated(USERNAME, "wrong-password"),
                new BadCredentialsException("Bad credentials"));
    }
}
