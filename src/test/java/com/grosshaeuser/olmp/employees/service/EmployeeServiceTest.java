package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.dto.*;
import com.grosshaeuser.olmp.employees.entity.*;
import com.grosshaeuser.olmp.employees.repository.*;
import com.grosshaeuser.olmp.employees.service.impl.EmployeeServiceImpl;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.*;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeAccountService accountService;
    @Mock private RoleRepository roleRepository;
    @Mock private AuthenticationContext authenticationContext;
    @InjectMocks private EmployeeServiceImpl service;

    @ParameterizedTest
    @ValueSource(strings = {ADMIN, STAFF})
    void searchAndCountUseReadableRoles(String role) {
        when(authenticationContext.hasAuthority(READ_ADMIN_ACCOUNT)).thenReturn(role.equals(ADMIN));
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(role.equals(STAFF));
        Pageable pageable = PageRequest.of(1, 2);
        Employee employee = employee(role);
        when(employeeRepository.searchWithRolesIn("Ada", Set.of(role), pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 3));
        when(employeeRepository.countSearch("Ada", Set.of(role))).thenReturn(3L);

        Page<EmployeeDTO> result = service.getEmployees("Ada", pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(pageable, result.getPageable());
        assertEquals(employee.getId(), result.getContent().getFirst().id());
        assertEquals(Set.of(role), result.getContent().getFirst().roles());
        assertEquals(3, service.countEmployees("Ada"));
    }

    @Test
    void searchAndCountRejectUsersWithoutReadPermission() {
        assertThrows(AccessDeniedException.class, () -> service.getEmployees("", Pageable.unpaged()));
        assertThrows(AccessDeniedException.class, () -> service.countEmployees(""));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void createPersistsEmployeeAndPassesAccountData() {
        allowCreate(STAFF);
        EmployeeRegistrationDTO dto = registration(STAFF);
        when(roleRepository.findAllByNameIn(dto.getRoles())).thenReturn(Set.of(role(STAFF)));
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(10L);
            return employee;
        });

        EmployeeDTO result = service.createEmployee(dto).orElseThrow();

        assertEquals(10L, result.id());
        assertEquals(dto.getEmail(), result.email());
        assertEquals(dto.getFirstName(), result.firstName());
        assertEquals(dto.getLastName(), result.lastName());
        assertEquals(Set.of(STAFF), result.roles());
        ArgumentCaptor<EmployeeAccountCreateDTO> captor = ArgumentCaptor.forClass(EmployeeAccountCreateDTO.class);
        verify(accountService).createEmployeeAccount(captor.capture());
        assertEquals(result.id(), captor.getValue().getEmployee().getId());
        assertEquals(dto.getUsername(), captor.getValue().getUsername());
        assertEquals(dto.getPassword(), captor.getValue().getPassword());
        assertEquals(dto.isEnabled(), captor.getValue().isEnabled());
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN, STAFF})
    void createRequiresBothCreateAndAssignmentPermissions(String role) {
        EmployeeRegistrationDTO dto = registration(role);
        assertThrows(AccessDeniedException.class, () -> service.createEmployee(dto));
        when(authenticationContext.hasAuthority(role.equals(ADMIN) ? CREATE_ADMIN_ACCOUNT : CREATE_STAFF_ACCOUNT)).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> service.createEmployee(dto));
        verifyNoInteractions(employeeRepository, roleRepository, accountService);
    }

    @Test
    void createRejectsDuplicateEmail() {
        allowCreate(STAFF);
        EmployeeRegistrationDTO dto = registration(STAFF);
        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> service.createEmployee(dto));
        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(accountService);
    }

    @Test
    void createAndUpdateRejectUnknownRoles() {
        EmployeeRegistrationDTO dto = registration("UNKNOWN");
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee(dto));
        assertThrows(IllegalArgumentException.class, () -> service.updateEmployee(
                EmployeeEditDTO.builder().roles(Set.of("UNKNOWN")).build()));
        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(accountService);
    }

    @Test
    void detailedLookupMergesEmployeeAndAccount() {
        Employee employee = employee(STAFF);
        EmployeeAccount account = account(employee);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(authenticationContext.hasAuthority(READ_ADMIN_ACCOUNT)).thenReturn(false);
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(true);
        when(accountService.findByEmployeeId(10L)).thenReturn(Optional.of(account));
        EmployeeEditDTO result = service.getDetailedEmployeeById(10L).orElseThrow();
        assertEquals(employee.getEmail(), result.getEmail());
        assertEquals(account.getId(), result.getAccountId());
        assertEquals(account.getUsername(), result.getUsername());
        assertEquals(Set.of(STAFF), result.getRoles());
    }

    @Test
    void detailedLookupReturnsEmptyForMissingEmployeeOrAccount() {
        assertTrue(service.getDetailedEmployeeById(99L).isEmpty());
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee(STAFF)));
        when(authenticationContext.hasAuthority(READ_ADMIN_ACCOUNT)).thenReturn(false);
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(true);
        assertTrue(service.getDetailedEmployeeById(10L).isEmpty());
    }

    @Test
    void detailedLookupRequiresPermissionForEveryRole() {
        Employee employee = employee(STAFF);
        employee.addRole(role(ADMIN));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(authenticationContext.hasAuthority(READ_ADMIN_ACCOUNT)).thenReturn(false);
        when(authenticationContext.hasAuthority(READ_STAFF_ACCOUNT)).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> service.getDetailedEmployeeById(10L));
        verifyNoInteractions(accountService);
    }

    @Test
    void updateChangesProfileRolesAndAccount() {
        Employee employee = employee(STAFF);
        EmployeeAccount account = account(employee);
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(true);
        when(authenticationContext.hasAuthority(UPDATE_STAFF_ACCOUNT)).thenReturn(true);
        when(roleRepository.findAllByNameIn(Set.of(ADMIN))).thenReturn(Set.of(role(ADMIN)));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(accountService.updateEmployeeAccount(any())).thenReturn(Optional.of(account));
        EmployeeEditDTO dto = edit().toBuilder().roles(Set.of(ADMIN)).firstName("Grace")
                .lastName("Hopper").email("grace@example.com").build();

        EmployeeEditDTO result = service.updateEmployee(dto).orElseThrow();

        assertEquals("Grace", result.getFirstName());
        assertEquals("Hopper", result.getLastName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(Set.of(ADMIN), result.getRoles());
        verify(accountService).updateEmployeeAccount(argThat(data -> data.getId().equals(dto.getAccountId())
                && data.getUsername().equals(dto.getUsername())));
    }

    @Test
    void updateRejectsMissingEmployee() {
        assertThrows(IllegalArgumentException.class, () -> service.updateEmployee(edit()));
        verifyNoInteractions(accountService);
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN, STAFF})
    void updateRequiresPermissionForExistingRole(String role) {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee(role)));
        assertThrows(AccessDeniedException.class, () -> service.updateEmployee(edit()));
        verify(employeeRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN, STAFF})
    void updateRequiresAssignmentPermission(String role) {
        assertThrows(AccessDeniedException.class, () -> service.updateEmployee(edit().toBuilder().roles(Set.of(role)).build()));
        verifyNoInteractions(employeeRepository, accountService);
    }

    @Test
    void updateRejectsDuplicateEmailBeforeChangingEmployee() {
        Employee employee = employee(STAFF);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(authenticationContext.hasAuthority(UPDATE_STAFF_ACCOUNT)).thenReturn(true);
        when(employeeRepository.existsByEmail("taken@example.com")).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> service.updateEmployee(
                edit().toBuilder().email("taken@example.com").build()));
        assertEquals("ada@example.com", employee.getEmail());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateFailsWhenAccountUpdateReturnsEmpty() {
        Employee employee = employee(STAFF);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(authenticationContext.hasAuthority(UPDATE_STAFF_ACCOUNT)).thenReturn(true);
        when(employeeRepository.save(employee)).thenReturn(employee);
        assertThrows(RuntimeException.class, () -> service.updateEmployee(edit()));
        verify(employeeRepository, never()).existsByEmail(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN, STAFF})
    void deleteRequiresRolePermission(String role) {
        Employee employee = employee(role);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        assertThrows(AccessDeniedException.class, () -> service.deleteEmployee(10L));
        verify(employeeRepository, never()).delete(any());
        when(authenticationContext.hasAuthority(role.equals(ADMIN) ? DELETE_ADMIN_ACCOUNT : DELETE_STAFF_ACCOUNT)).thenReturn(true);
        service.deleteEmployee(10L);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteRejectsMissingEmployee() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteEmployee(99L));
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void currentEmployeeRequiresAuthenticationAndExistingAccount() {
        assertThrows(AccessDeniedException.class, service::getCurrentEmployee);
        assertThrows(AccessDeniedException.class, () -> service.updateCurrentEmployee(edit()));
        when(authenticationContext.getAuthenticatedUser(UserDetails.class))
                .thenReturn(Optional.of(User.withUsername("ada").password("unused").authorities("unused").build()));
        assertThrows(AccessDeniedException.class, service::getCurrentEmployee);
    }

    @Test
    void currentEmployeeLoadsProfileAndRejectsMissingProfile() {
        Employee employee = employee(STAFF);
        authenticate(account(employee));
        assertThrows(AccessDeniedException.class, service::getCurrentEmployee);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        EmployeeEditDTO result = service.getCurrentEmployee().orElseThrow();
        assertEquals(10L, result.getId());
        assertEquals(20L, result.getAccountId());
    }

    @Test
    void selfUpdateRejectsOtherEmployeeAndAccountIds() {
        authenticate(account(employee(STAFF)));
        assertThrows(AccessDeniedException.class, () -> service.updateCurrentEmployee(edit().toBuilder().id(99L).build()));
        assertThrows(AccessDeniedException.class, () -> service.updateCurrentEmployee(edit().toBuilder().accountId(99L).build()));
        verify(employeeRepository, never()).save(any());
        verify(accountService, never()).updateEmployeeAccount(any());
    }

    @Test
    void selfUpdatePreservesRolesAndAccountSecurityState() {
        Employee employee = employee(STAFF);
        EmployeeAccount account = account(employee);
        account.setFailedLoginAttempts(3);
        account.setLockoutUntil(OffsetDateTime.now().plusHours(1));
        authenticate(account);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(accountService.updateEmployeeAccount(any())).thenReturn(Optional.of(account));
        EmployeeEditDTO dto = edit().toBuilder().firstName("Grace").roles(Set.of(ADMIN))
                .enabled(false).locked(true).failedLoginAttempts(0).build();

        EmployeeEditDTO result = service.updateCurrentEmployee(dto).orElseThrow();

        assertEquals("Grace", result.getFirstName());
        assertEquals(Set.of(STAFF), result.getRoles());
        ArgumentCaptor<EmployeeAccountEditDTO> captor = ArgumentCaptor.forClass(EmployeeAccountEditDTO.class);
        verify(accountService).updateEmployeeAccount(captor.capture());
        EmployeeAccountEditDTO sent = captor.getValue();
        assertTrue(sent.isEnabled());
        assertFalse(sent.isLocked());
        assertEquals(3, sent.getFailedLoginAttempts());
        assertEquals(account.getLockoutUntil().toLocalDateTime(), sent.getLockoutUntil());
        assertEquals(Set.of(ADMIN), dto.getRoles());
        verify(employeeRepository, never()).existsByEmail(any());
    }

    @Test
    void selfUpdateRejectsDuplicateEmail() {
        authenticate(account(employee(STAFF)));
        when(employeeRepository.existsByEmail("taken@example.com")).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> service.updateCurrentEmployee(
                edit().toBuilder().email("taken@example.com").build()));
        verify(employeeRepository, never()).save(any());
    }

    private void authenticate(EmployeeAccount account) {
        when(authenticationContext.getAuthenticatedUser(UserDetails.class))
                .thenReturn(Optional.of(User.withUsername("ada").password("unused").authorities("unused").build()));
        when(accountService.findByUsername("ada")).thenReturn(Optional.of(account));
    }

    private void allowCreate(String role) {
        when(authenticationContext.hasAuthority(role.equals(ADMIN) ? CREATE_ADMIN_ACCOUNT : CREATE_STAFF_ACCOUNT)).thenReturn(true);
        when(authenticationContext.hasAuthority(role.equals(ADMIN) ? ASSIGN_ADMIN_ROLE : ASSIGN_STAFF_ROLE)).thenReturn(true);
    }

    private Role role(String name) { return Role.builder().name(name).build(); }

    private Employee employee(String role) {
        return Employee.builder().id(10L).firstName("Ada").lastName("Lovelace")
                .email("ada@example.com").roles(new HashSet<>(Set.of(role(role)))).build();
    }

    private EmployeeAccount account(Employee employee) {
        return EmployeeAccount.builder().id(20L).employee(employee).username("ada").enabled(true).build();
    }

    private EmployeeEditDTO edit() {
        return EmployeeEditDTO.builder().id(10L).accountId(20L).firstName("Ada").lastName("Lovelace")
                .email("ada@example.com").username("ada").enabled(true).build();
    }

    private EmployeeRegistrationDTO registration(String role) {
        EmployeeRegistrationDTO dto = new EmployeeRegistrationDTO();
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        dto.setEmail("ada@example.com");
        dto.setUsername("ada");
        dto.setPassword("Password1!");
        dto.setRoles(Set.of(role));
        return dto;
    }
}
