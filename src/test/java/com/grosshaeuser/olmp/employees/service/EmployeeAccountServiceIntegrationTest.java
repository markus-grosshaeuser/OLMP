package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employees.repository.EmployeeRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EmployeeAccountServiceIntegrationTest {
    @Autowired
    private EmployeeAccountService serviceUnderTest;
    @Autowired
    private EmployeeAccountRepository accountRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private AuthenticationContext authenticationContext;

    @Test
    void lookupsReturnEmptyForMissingAccount() {
        assertTrue(serviceUnderTest.findById(Long.MAX_VALUE).isEmpty());
        assertTrue(serviceUnderTest.findByUsername("missing-employee-account").isEmpty());
    }

    @Test
    void lookupsReturnPersistedAccount() {
        EmployeeAccount account = persistAccount("lookup-employee");
        Long id = account.getId();
        flushAndClear();

        EmployeeAccount byId = serviceUnderTest.findById(id).orElseThrow();
        EmployeeAccount byUsername = serviceUnderTest.findByUsername("lookup-employee").orElseThrow();

        assertEquals(id, byUsername.getId());
        assertEquals("lookup-employee", byId.getUsername());
        assertEquals(account.getEmployee().getId(), byId.getEmployee().getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createPersistsAccountWithEncodedPassword(boolean enabled) {
        Employee employee = persistEmployee();
        EmployeeAccountCreateDTO dto = new EmployeeAccountCreateDTO(employee, "created-employee", "Password1!", enabled);

        EmployeeAccount created = serviceUnderTest.createEmployeeAccount(dto).orElseThrow();
        Long id = created.getId();
        flushAndClear();
        EmployeeAccount stored = accountRepository.findById(id).orElseThrow();

        assertEquals(employee.getId(), stored.getEmployee().getId());
        assertEquals(dto.getUsername(), stored.getUsername());
        assertEquals(enabled, stored.isEnabled());
        assertFalse(stored.isLocked());
        assertEquals(0, stored.getFailedLoginAttempts());
        assertNotEquals(dto.getPassword(), stored.getPasswordHash());
        assertTrue(passwordEncoder.matches(dto.getPassword(), stored.getPasswordHash()));
        assertNotNull(stored.getCreatedAt());
        assertNotNull(stored.getUpdatedAt());
    }

    @Test
    void createRejectsDuplicateUsernameWithoutPersistingAnotherAccount() {
        persistAccount("taken-employee");
        Employee employee = persistEmployee();
        long count = accountRepository.count();

        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.createEmployeeAccount(
                new EmployeeAccountCreateDTO(employee, "taken-employee", "Password1!", true)));

        flushAndClear();
        assertEquals(count, accountRepository.count());
    }

    @Test
    void createRejectsInvalidPasswordWithoutPersistingAccount() {
        Employee employee = persistEmployee();
        long count = accountRepository.count();

        assertThrows(IllegalArgumentException.class, () -> serviceUnderTest.createEmployeeAccount(
                new EmployeeAccountCreateDTO(employee, "invalid-password", "weak", true)));

        flushAndClear();
        assertEquals(count, accountRepository.count());
    }

    @Test
    void updateRejectsMissingAccount() {
        EmployeeAccountEditDTO dto = EmployeeAccountEditDTO.builder().id(Long.MAX_VALUE)
                .username("missing-employee-account").build();
        assertThrows(IllegalArgumentException.class, () -> serviceUnderTest.updateEmployeeAccount(dto));
    }

    @Test
    void updatePersistsUsernamePasswordAndStatusChanges() {
        EmployeeAccount account = persistAccount("old-employee");
        Long employeeId = account.getEmployee().getId();
        when(authenticationContext.hasAuthority(RESET_PASSWORD)).thenReturn(true);
        when(authenticationContext.hasAuthority(DISABLE_USER)).thenReturn(true);
        when(authenticationContext.hasAuthority(LOCK_USER)).thenReturn(true);
        EmployeeAccountEditDTO dto = edit(account).toBuilder().username("updated-employee")
                .newPassword("NewPassword123!").enabled(false).locked(true).build();

        EmployeeAccount updated = serviceUnderTest.updateEmployeeAccount(dto).orElseThrow();
        assertEquals(account.getId(), updated.getId());
        flushAndClear();
        EmployeeAccount stored = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(employeeId, stored.getEmployee().getId());
        assertEquals("updated-employee", stored.getUsername());
        assertTrue(passwordEncoder.matches(dto.getNewPassword(), stored.getPasswordHash()));
        assertFalse(stored.isEnabled());
        assertTrue(stored.isLocked());
        assertTrue(serviceUnderTest.findByUsername("old-employee").isEmpty());
        assertEquals(stored.getId(), serviceUnderTest.findByUsername("updated-employee").orElseThrow().getId());
    }

    @ParameterizedTest
    @CsvSource({ENABLE_USER + ",false,false,true,false", DISABLE_USER + ",true,false,false,false",
            LOCK_USER + ",true,false,true,true", UNLOCK_USER + ",true,true,true,false"})
    void updatePersistsAuthorizedStatusTransition(String privilege, boolean enabled, boolean locked,
                                                 boolean requestedEnabled, boolean requestedLocked) {
        EmployeeAccount account = persistAccount("status-employee");
        account.setEnabled(enabled);
        account.setLocked(locked);
        flushAndClear();
        when(authenticationContext.hasAuthority(privilege)).thenReturn(true);

        serviceUnderTest.updateEmployeeAccount(edit(account).toBuilder()
                .enabled(requestedEnabled).locked(requestedLocked).build());
        flushAndClear();

        EmployeeAccount stored = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(requestedEnabled, stored.isEnabled());
        assertEquals(requestedLocked, stored.isLocked());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void updatePreservesHashWhenNewPasswordIsAbsentOrBlank(String password) {
        EmployeeAccount account = persistAccount("unchanged-employee");
        String originalHash = account.getPasswordHash();

        serviceUnderTest.updateEmployeeAccount(edit(account).toBuilder().newPassword(password).build());
        flushAndClear();

        EmployeeAccount stored = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(originalHash, stored.getPasswordHash());
        assertEquals("unchanged-employee", stored.getUsername());
    }

    @Test
    void updateRejectsUsernameOwnedByAnotherAccount() {
        persistAccount("taken-employee");
        EmployeeAccount account = persistAccount("original-employee");

        assertThrows(DataIntegrityViolationException.class, () -> serviceUnderTest.updateEmployeeAccount(
                edit(account).toBuilder().username("taken-employee").build()));
        flushAndClear();

        assertEquals("original-employee", accountRepository.findById(account.getId()).orElseThrow().getUsername());
    }

    private Employee persistEmployee() {
        return employeeRepository.saveAndFlush(Employee.builder().firstName("John").lastName("Doe").build());
    }

    private EmployeeAccount persistAccount(String username) {
        return accountRepository.saveAndFlush(EmployeeAccount.builder().employee(persistEmployee())
                .username(username).passwordHash(passwordEncoder.encode("Password1!"))
                .enabled(true).locked(false).build());
    }

    private EmployeeAccountEditDTO edit(EmployeeAccount account) {
        return EmployeeAccountEditDTO.builder().id(account.getId()).username(account.getUsername())
                .enabled(account.isEnabled()).locked(account.isLocked()).build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
