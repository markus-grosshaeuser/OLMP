package com.grosshaeuser.olmp.employees.mapper;

import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeRegistrationDTO;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    @Test
    void mapEmployeeToEmployeeDtoMapsAllFieldsAndConvertsTimestamps() {
        EmployeeDTO result = EmployeeMapper.mapEmployeeToEmployeeDto(employee());

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("jane.doe@example.org");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.roles()).containsExactlyInAnyOrder("ADMIN", "EMPLOYEE");
        assertThat(result.creationTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 0));
        assertThat(result.lastModifiedTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 0));
    }

    @Test
    void mapEmployeeToEmployeeDtoPreservesNullTimestampsAndEmptyRoles() {
        EmployeeDTO result = EmployeeMapper.mapEmployeeToEmployeeDto(Employee.builder().build());

        assertThat(result.creationTimestamp()).isNull();
        assertThat(result.lastModifiedTimestamp()).isNull();
        assertThat(result.roles()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mergeEmployeeAndEmployeeAccountToEmployeeEditDtoMapsAllFields(boolean enabled) {
        Employee employee = employee();
        EmployeeAccount account = EmployeeAccount.builder()
                .id(20L)
                .employee(employee)
                .username("jane.doe")
                .passwordHash("stored-password-hash")
                .enabled(enabled)
                .failedLoginAttempts(3)
                .lockoutUntil(OffsetDateTime.parse("2026-10-13T12:00:00+02:00"))
                .locked(!enabled)
                .createdAt(OffsetDateTime.parse("2026-10-12T10:15:00+02:00"))
                .updatedAt(OffsetDateTime.parse("2026-10-12T11:15:00+02:00"))
                .lastAccess(OffsetDateTime.parse("2026-10-12T12:15:00+02:00"))
                .build();

        EmployeeEditDTO result = EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(employee, account);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.org");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 0));
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 0));
        assertThat(result.getAccountId()).isEqualTo(20L);
        assertThat(result.getUsername()).isEqualTo("jane.doe");
        assertThat(result.getNewPassword()).isEmpty();
        assertThat(result.isEnabled()).isEqualTo(enabled);
        assertThat(result.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(result.getLockoutUntil()).isEqualTo(LocalDateTime.of(2026, 10, 13, 12, 0));
        assertThat(result.isLocked()).isEqualTo(!enabled);
        assertThat(result.getAccountCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 15));
        assertThat(result.getAccountUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 15));
        assertThat(result.getAccountLastAccessed()).isEqualTo(LocalDateTime.of(2026, 10, 12, 12, 15));
        assertThat(result.getRoles()).containsExactlyInAnyOrder("ADMIN", "EMPLOYEE");
    }

    @Test
    void mergeEmployeeAndEmployeeAccountToEmployeeEditDtoPreservesNullTimestampsAndEmptyRoles() {
        EmployeeEditDTO result = EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(
                Employee.builder().build(), EmployeeAccount.builder().build());

        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getLockoutUntil()).isNull();
        assertThat(result.getAccountCreatedAt()).isNull();
        assertThat(result.getAccountUpdatedAt()).isNull();
        assertThat(result.getAccountLastAccessed()).isNull();
        assertThat(result.getNewPassword()).isEmpty();
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void createEmployeeFromEmployeeRegistrationDataMapsPersonalDetailsAndSuppliedRoles() {
        EmployeeRegistrationDTO dto = registrationDto();
        dto.setRoles(Set.of("IGNORED_REGISTRATION_ROLE"));
        Set<Role> roles = Set.of(Role.builder().name("EMPLOYEE").build());

        Employee result = EmployeeMapper.createEmployeeFromEmployeeRegistrationData(dto, roles);

        assertThat(result.getId()).isNull();
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.org");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getRoles()).containsExactlyElementsOf(roles);
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createEmployeeAccountDTOFromEmployeeRegistrationDataMapsAccountFields(boolean enabled) {
        EmployeeRegistrationDTO dto = registrationDto();
        dto.setEnabled(enabled);
        Employee employee = employee();

        EmployeeAccountCreateDTO result = EmployeeMapper.createEmployeeAccountDTOFromEmployeeRegistrationData(dto, employee);

        assertThat(result.getEmployee()).isSameAs(employee);
        assertThat(result.getUsername()).isEqualTo("jane.doe");
        assertThat(result.getPassword()).isEqualTo("plain-password");
        assertThat(result.isEnabled()).isEqualTo(enabled);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createAccountEditDtoFromEmployeeUpdateDataMapsAccountFields(boolean enabled) {
        EmployeeEditDTO dto = EmployeeEditDTO.builder()
                .id(1L)
                .accountId(20L)
                .username("jane.doe")
                .newPassword("new-password")
                .enabled(enabled)
                .failedLoginAttempts(3)
                .lockoutUntil(LocalDateTime.of(2026, 10, 13, 12, 0))
                .locked(!enabled)
                .build();

        EmployeeAccountEditDTO result = EmployeeMapper.createAccountEditDtoFromEmployeeUpdateData(dto);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getUsername()).isEqualTo("jane.doe");
        assertThat(result.getNewPassword()).isEqualTo("new-password");
        assertThat(result.isEnabled()).isEqualTo(enabled);
        assertThat(result.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(result.getLockoutUntil()).isEqualTo(LocalDateTime.of(2026, 10, 13, 12, 0));
        assertThat(result.isLocked()).isEqualTo(!enabled);
    }

    @Test
    void createAccountEditDtoFromEmployeeUpdateDataPreservesEmptyPasswordAndNullLockout() {
        EmployeeAccountEditDTO result = EmployeeMapper.createAccountEditDtoFromEmployeeUpdateData(new EmployeeEditDTO());

        assertThat(result.getNewPassword()).isEmpty();
        assertThat(result.getLockoutUntil()).isNull();
        assertThat(result.getFailedLoginAttempts()).isZero();
    }

    @Test
    void mapRolesToRoleNamesMapsNamesWithoutDependingOnOrder() {
        Set<Role> roles = Set.of(
                Role.builder().name("ADMIN").build(),
                Role.builder().name("EMPLOYEE").build());

        assertThat(EmployeeMapper.mapRolesToRoleNames(roles)).containsExactlyInAnyOrder("ADMIN", "EMPLOYEE");
    }

    @Test
    void mapRolesToRoleNamesReturnsEmptySetForEmptyRoles() {
        assertThat(EmployeeMapper.mapRolesToRoleNames(Set.of())).isEmpty();
    }

    private static Employee employee() {
        return Employee.builder()
                .id(1L)
                .email("jane.doe@example.org")
                .lastName("Doe")
                .firstName("Jane")
                .roles(Set.of(Role.builder().name("ADMIN").build(), Role.builder().name("EMPLOYEE").build()))
                .createdAt(OffsetDateTime.parse("2026-10-12T10:00:00+02:00"))
                .updatedAt(OffsetDateTime.parse("2026-10-12T11:00:00+02:00"))
                .build();
    }

    private static EmployeeRegistrationDTO registrationDto() {
        EmployeeRegistrationDTO dto = new EmployeeRegistrationDTO();
        dto.setEmail("jane.doe@example.org");
        dto.setLastName("Doe");
        dto.setFirstName("Jane");
        dto.setUsername("jane.doe");
        dto.setPassword("plain-password");
        return dto;
    }
}
