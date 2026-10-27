package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.dto.*;
import com.grosshaeuser.olmp.employees.entity.*;
import com.grosshaeuser.olmp.employees.repository.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EmployeeServiceIntegrationTest {
    @Autowired private EmployeeService service;
    @Autowired private EmployeeRepository employees;
    @Autowired private EmployeeAccountRepository accounts;
    @Autowired private RoleRepository roles;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private AuthenticationContext authenticationContext;


    @Test
    void createPersistsEmployeeRolesAndEncodedAccount() {
        allowCreate();
        EmployeeRegistrationDTO dto = registration("service-created");
        EmployeeDTO created = service.createEmployee(dto).orElseThrow();
        flushAndClear();

        Employee employee = employees.findById(created.id()).orElseThrow();
        EmployeeAccount account = accounts.findByUsername(dto.getUsername()).orElseThrow();
        assertEquals(dto.getFirstName(), employee.getFirstName());
        assertEquals(dto.getLastName(), employee.getLastName());
        assertEquals(dto.getEmail(), employee.getEmail());
        assertTrue(employee.hasRole(STAFF));
        assertEquals(employee.getId(), account.getEmployee().getId());
        assertTrue(account.isEnabled());
        assertTrue(passwordEncoder.matches(dto.getPassword(), account.getPasswordHash()));
        assertNotEquals(dto.getPassword(), account.getPasswordHash());
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
    }

    @Test
    void searchFiltersByQueryAndReadableRoleAndSupportsPagination() {
        persist("service-search-a", STAFF);
        persist("service-search-b", STAFF);
        persist("service-search-admin", ADMIN);
        persist("unrelated", STAFF);
        flushAndClear();
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(true);

        Page<EmployeeDTO> page = service.getEmployees("SERVICE-SEARCH", PageRequest.of(0, 1, Sort.by("email")));
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getNumberOfElements());
        assertEquals("service-search-a@example.com", page.getContent().getFirst().email());
        assertEquals(2, service.countEmployees("SERVICE-SEARCH"));
        assertEquals("service-search-b@example.com", service.getEmployees("service-search",
                PageRequest.of(1, 1, Sort.by("email"))).getContent().getFirst().email());
        when(authenticationContext.hasAuthority(READ_ADMIN_ACCOUNT)).thenReturn(true);
        assertEquals(3, service.countEmployees("service-search"));
    }

    @Test
    void detailedLookupCombinesProfileAndAccountAndEnforcesReadPermission() {
        EmployeeAccount account = persist("service-details", STAFF);
        Long id = account.getEmployee().getId();
        flushAndClear();
        assertThrows(AccessDeniedException.class, () -> service.getDetailedEmployeeById(id));
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(true);
        EmployeeEditDTO result = service.getDetailedEmployeeById(id).orElseThrow();
        assertEquals(id, result.getId());
        assertEquals(account.getId(), result.getAccountId());
        assertEquals("service-details", result.getUsername());
        assertEquals(Set.of(STAFF), result.getRoles());
        assertTrue(service.getDetailedEmployeeById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void updatePersistsProfileRoleAndAccountChanges() {
        EmployeeAccount account = persist("service-update", STAFF);
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(true);
        when(authenticationContext.hasAuthority(UPDATE_STAFF_ACCOUNT)).thenReturn(true);
        when(authenticationContext.hasAuthority(RESET_PASSWORD)).thenReturn(true);
        EmployeeEditDTO dto = edit(account).toBuilder().firstName("Grace").lastName("Hopper")
                .email("service-updated@example.com").username("service-updated")
                .roles(Set.of(ADMIN)).newPassword("NewPassword123!").build();

        service.updateEmployee(dto).orElseThrow();
        flushAndClear();

        Employee stored = employees.findById(dto.getId()).orElseThrow();
        EmployeeAccount storedAccount = accounts.findById(account.getId()).orElseThrow();
        assertEquals("Grace", stored.getFirstName());
        assertEquals("Hopper", stored.getLastName());
        assertEquals(dto.getEmail(), stored.getEmail());
        assertEquals(Set.of(ADMIN), stored.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
        assertEquals(dto.getUsername(), storedAccount.getUsername());
        assertTrue(passwordEncoder.matches(dto.getNewPassword(), storedAccount.getPasswordHash()));
    }

    @Test
    void duplicateEmailIsRejectedDuringCreation() {
        EmployeeAccount account = persist("service-duplicate", STAFF);
        allowCreate();
        EmployeeRegistrationDTO dto = registration("service-other");
        dto.setEmail(account.getEmployee().getEmail());
        long count = employees.count();
        assertThrows(DataIntegrityViolationException.class, () -> service.createEmployee(dto));
        // The service transaction is marked for rollback, so inspect without committing it.
        assertEquals(count, employees.count());
        assertFalse(accounts.existsByUsername(dto.getUsername()));
    }

    @Test
    void accountCreationFailureRollsBackNewEmployee() {
        allowCreate();
        EmployeeRegistrationDTO dto = registration("service-rollback");
        dto.setPassword("weak");
        long employeeCount = employees.count();
        long accountCount = accounts.count();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThrows(IllegalArgumentException.class, () -> transaction.execute(status -> service.createEmployee(dto)));
        flushAndClear();

        assertEquals(employeeCount, employees.count());
        assertEquals(accountCount, accounts.count());
        assertFalse(employees.existsByEmail(dto.getEmail()));
        assertFalse(accounts.existsByUsername(dto.getUsername()));
    }

    @Test
    void deleteRemovesEmployeeAccountAndRoleLinks() {
        EmployeeAccount account = persist("service-delete", STAFF);
        flushAndClear();
        Long employeeId = account.getEmployee().getId();
        Long accountId = account.getId();
        when(authenticationContext.hasAuthority(DELETE_STAFF_ACCOUNT)).thenReturn(true);
        service.deleteEmployee(employeeId);
        flushAndClear();
        assertFalse(employees.existsById(employeeId));
        assertFalse(accounts.existsById(accountId));
        assertTrue(roles.findByName(STAFF).isPresent());
        Number links = (Number) entityManager.createNativeQuery(
                "select count(*) from employee_roles where employee_id = :id")
                .setParameter("id", employeeId).getSingleResult();
        assertEquals(0, links.longValue());
    }

    @Test
    void selfServiceUpdatesProfileWithoutEscalatingRolesOrChangingSecurityState() {
        EmployeeAccount account = persist("service-self", STAFF);
        account.setFailedLoginAttempts(3);
        account.setLockoutUntil(OffsetDateTime.now().plusHours(1));
        accounts.saveAndFlush(account);
        Long accountId = account.getId();
        Long employeeId = account.getEmployee().getId();
        when(authenticationContext.getAuthenticatedUser(UserDetails.class)).thenReturn(Optional.of(
                User.withUsername(account.getUsername()).password("unused").authorities("unused").build()));
        flushAndClear();
        EmployeeEditDTO current = service.getCurrentEmployee().orElseThrow();
        assertEquals(employeeId, current.getId());

        service.updateCurrentEmployee(current.toBuilder().firstName("Updated").username("service-self-updated")
                .roles(Set.of(ADMIN)).enabled(false).locked(true).failedLoginAttempts(0).lockoutUntil(null).build());
        flushAndClear();

        Employee stored = employees.findById(employeeId).orElseThrow();
        EmployeeAccount storedAccount = accounts.findById(accountId).orElseThrow();
        assertEquals("Updated", stored.getFirstName());
        assertTrue(stored.hasRole(STAFF));
        assertFalse(stored.hasRole(ADMIN));
        assertEquals("service-self-updated", storedAccount.getUsername());
        assertTrue(storedAccount.isEnabled());
        assertFalse(storedAccount.isLocked());
        assertEquals(3, storedAccount.getFailedLoginAttempts());
        assertNotNull(storedAccount.getLockoutUntil());
    }

    private void allowCreate() {
        when(authenticationContext.hasAuthority(CREATE_STAFF_ACCOUNT)).thenReturn(true);
        when(authenticationContext.hasAuthority(ASSIGN_STAFF_ROLE)).thenReturn(true);
    }

    private EmployeeAccount persist(String username, String role) {
        Employee employee = employees.saveAndFlush(Employee.builder().firstName("Ada").lastName("Lovelace")
                .email(username + "@example.com").roles(new HashSet<>(Set.of(roles.findByName(role).orElseThrow()))).build());
        return accounts.saveAndFlush(EmployeeAccount.builder().employee(employee).username(username)
                .passwordHash(passwordEncoder.encode("Password1!")).enabled(true).build());
    }

    private EmployeeRegistrationDTO registration(String username) {
        EmployeeRegistrationDTO dto = new EmployeeRegistrationDTO();
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        dto.setEmail(username + "@example.com");
        dto.setUsername(username);
        dto.setPassword("Password1!");
        dto.setRoles(Set.of(STAFF));
        return dto;
    }

    private EmployeeEditDTO edit(EmployeeAccount account) {
        Employee employee = account.getEmployee();
        return EmployeeEditDTO.builder().id(employee.getId()).accountId(account.getId())
                .firstName(employee.getFirstName()).lastName(employee.getLastName()).email(employee.getEmail())
                .username(account.getUsername()).enabled(account.isEnabled()).locked(account.isLocked())
                .roles(Set.of(STAFF)).build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
