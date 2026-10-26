package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employees.service.impl.EmployeeAccountServiceImpl;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeAccountServiceTest {
    @Mock
    private EmployeeAccountRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationContext authenticationContext;

    private final PasswordProperties passwordProperties = new PasswordProperties(8, true, true, true);
    private EmployeeAccountServiceImpl serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new EmployeeAccountServiceImpl(repository, passwordEncoder,
                passwordProperties, authenticationContext);
    }

    @Test
    void findByIdReturnsRepositoryResult() {
        EmployeeAccount account = account();
        when(repository.findById(42L)).thenReturn(Optional.of(account));
        assertSame(account, serviceUnderTest.findById(42L).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyForMissingAccount() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertTrue(serviceUnderTest.findById(42L).isEmpty());
    }

    @Test
    void findByUsernameReturnsRepositoryResult() {
        EmployeeAccount account = account();
        when(repository.findByUsername("employee")).thenReturn(Optional.of(account));
        assertSame(account, serviceUnderTest.findByUsername("employee").orElseThrow());
    }

    @Test
    void findByUsernameReturnsEmptyForMissingAccount() {
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());
        assertTrue(serviceUnderTest.findByUsername("missing").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createSavesProvidedEmployeeAndUsernameWithEncodedPassword(boolean enabled) {
        Employee employee = Employee.builder().id(7L).build();
        EmployeeAccountCreateDTO dto = new EmployeeAccountCreateDTO(employee, "employee", "Password1!", enabled);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-password");
        when(repository.save(any(EmployeeAccount.class))).thenAnswer(invocation -> {
            EmployeeAccount saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        EmployeeAccount result = serviceUnderTest.createEmployeeAccount(dto).orElseThrow();

        assertEquals(42L, result.getId());
        assertSame(employee, result.getEmployee());
        assertEquals("employee", result.getUsername());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(enabled, result.isEnabled());
        assertFalse(result.isLocked());
        verify(repository).existsByUsername("employee");
        verify(repository).save(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "password1!", "PASSWORD1!", "Password!!", "Password12"})
    void createRejectsInvalidPasswordBeforeSaving(String password) {
        EmployeeAccountCreateDTO dto = new EmployeeAccountCreateDTO(null, "employee", password, true);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> serviceUnderTest.createEmployeeAccount(dto));
        assertEquals(passwordProperties.getPasswordRequirementDescription(), error.getMessage());
        verifyNoInteractions(repository, passwordEncoder);
    }

    @Test
    void createRejectsDuplicateUsername() {
        when(repository.existsByUsername("employee")).thenReturn(true);
        EmployeeAccountCreateDTO dto = new EmployeeAccountCreateDTO(null, "employee", "Password1!", true);
        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.createEmployeeAccount(dto));
        verify(repository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateRejectsMissingAccount() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> serviceUnderTest.updateEmployeeAccount(edit()));
        assertEquals("Account not found: 42", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsDuplicateUsername() {
        when(repository.findById(42L)).thenReturn(Optional.of(account()));
        when(repository.existsByUsername("taken")).thenReturn(true);
        EmployeeAccountEditDTO dto = edit().toBuilder().username("taken").build();
        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.updateEmployeeAccount(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void updateChangesAvailableUsernameWithoutAdditionalPermission() {
        EmployeeAccount account = givenExistingAccount();
        EmployeeAccountEditDTO dto = edit().toBuilder().username("renamed").build();

        assertSame(account, serviceUnderTest.updateEmployeeAccount(dto).orElseThrow());

        assertEquals("renamed", account.getUsername());
        verify(repository).existsByUsername("renamed");
        verify(repository).save(account);
        verifyNoInteractions(authenticationContext, passwordEncoder);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void updatePreservesPasswordAndSkipsUniquenessCheckForUnchangedUsername(String password) {
        EmployeeAccount account = givenExistingAccount();
        serviceUnderTest.updateEmployeeAccount(edit().toBuilder().newPassword(password).build());
        assertEquals("old-hash", account.getPasswordHash());
        verify(repository).save(account);
        verify(repository, never()).existsByUsername(any());
        verifyNoInteractions(authenticationContext, passwordEncoder);
    }

    @Test
    void updateEncodesNewPasswordWhenAuthorized() {
        EmployeeAccount account = givenExistingAccount();
        when(authenticationContext.hasAuthority(RESET_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new-hash");
        serviceUnderTest.updateEmployeeAccount(edit().toBuilder().newPassword("NewPassword1!").build());
        assertEquals("new-hash", account.getPasswordHash());
        verify(repository).save(account);
    }

    @Test
    void updateRejectsPasswordResetWithoutPermission() {
        when(repository.findById(42L)).thenReturn(Optional.of(account()));
        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.updateEmployeeAccount(
                edit().toBuilder().newPassword("NewPassword1!").build()));
        verify(authenticationContext).hasAuthority(RESET_PASSWORD);
        verify(repository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateRejectsInvalidNewPasswordWhenAuthorized() {
        when(repository.findById(42L)).thenReturn(Optional.of(account()));
        when(authenticationContext.hasAuthority(RESET_PASSWORD)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> serviceUnderTest.updateEmployeeAccount(
                edit().toBuilder().newPassword("weak").build()));
        verify(repository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @ParameterizedTest
    @CsvSource({ENABLE_USER + ",false,false,true,false", DISABLE_USER + ",true,false,false,false",
            LOCK_USER + ",true,false,true,true", UNLOCK_USER + ",true,true,true,false"})
    void updateAppliesStatusChangeWhenAuthorized(String privilege, boolean enabled, boolean locked,
                                               boolean requestedEnabled, boolean requestedLocked) {
        EmployeeAccount account = givenExistingAccount();
        account.setEnabled(enabled);
        account.setLocked(locked);
        when(authenticationContext.hasAuthority(privilege)).thenReturn(true);

        serviceUnderTest.updateEmployeeAccount(edit().toBuilder()
                .enabled(requestedEnabled).locked(requestedLocked).build());

        assertEquals(requestedEnabled, account.isEnabled());
        assertEquals(requestedLocked, account.isLocked());
        verify(authenticationContext).hasAuthority(privilege);
        verify(repository).save(account);
    }

    @ParameterizedTest
    @CsvSource({ENABLE_USER + ",false,false,true,false", DISABLE_USER + ",true,false,false,false",
            LOCK_USER + ",true,false,true,true", UNLOCK_USER + ",true,true,true,false"})
    void updateRejectsStatusChangeWithoutPermission(String privilege, boolean enabled, boolean locked,
                                                  boolean requestedEnabled, boolean requestedLocked) {
        EmployeeAccount account = account();
        account.setEnabled(enabled);
        account.setLocked(locked);
        when(repository.findById(42L)).thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class, () -> serviceUnderTest.updateEmployeeAccount(
                edit().toBuilder().enabled(requestedEnabled).locked(requestedLocked).build()));

        verify(authenticationContext).hasAuthority(privilege);
        verify(repository, never()).save(any());
    }

    private EmployeeAccount givenExistingAccount() {
        EmployeeAccount account = account();
        when(repository.findById(42L)).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        return account;
    }

    private EmployeeAccount account() {
        return EmployeeAccount.builder().id(42L).username("employee").passwordHash("old-hash")
                .enabled(true).locked(false).build();
    }

    private EmployeeAccountEditDTO edit() {
        return EmployeeAccountEditDTO.builder().id(42L).username("employee").enabled(true).locked(false).build();
    }

}
