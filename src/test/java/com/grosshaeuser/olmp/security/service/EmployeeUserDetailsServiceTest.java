package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Privilege;
import com.grosshaeuser.olmp.employees.entity.Role;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeUserDetailsServiceTest {

    @Mock
    private EmployeeAccountRepository repository;
    @InjectMocks
    private EmployeeUserDetailsService service;

    @Test
    void mapsRolesAndDeduplicatesSharedPrivileges() {
        Privilege read = new Privilege();
        read.setName("MEMBER_READ");
        Privilege edit = new Privilege();
        edit.setName("MEMBER_EDIT");
        Role librarian = new Role();
        librarian.setName("LIBRARIAN");
        librarian.addPrivilege(read);
        Role administrator = new Role();
        administrator.setName("ADMIN");
        administrator.addPrivilege(read);
        administrator.addPrivilege(edit);
        EmployeeAccount account = account();
        account.getEmployee().addRole(librarian);
        account.getEmployee().addRole(administrator);
        when(repository.findByUsername("employee")).thenReturn(Optional.of(account));

        var principal = service.loadUserByUsername("employee");

        assertThat(principal.getAccountId()).isEqualTo(11L);
        assertThat(principal.getEmployeeId()).isEqualTo(22L);
        assertThat(principal.getUsername()).isEqualTo("employee");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_LIBRARIAN", "ROLE_ADMIN", "MEMBER_READ", "MEMBER_EDIT");
    }

    @Test
    void employeeWithNoRolesHasNoAuthorities() {
        when(repository.findByUsername("employee")).thenReturn(Optional.of(account()));

        assertThat(service.loadUserByUsername("employee").getAuthorities()).isEmpty();
    }

    @Test
    void disabledOrLockedAccountIsReturnedForAuthenticationProviderToReject() {
        EmployeeAccount account = account();
        account.setEnabled(false);
        account.setLocked(true);
        when(repository.findByUsername("employee")).thenReturn(Optional.of(account));

        var principal = service.loadUserByUsername("employee");

        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.isAccountNonLocked()).isFalse();
    }

    @Test
    void missingEmployeeThrowsUsernameNotFound() {
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private EmployeeAccount account() {
        Employee employee = new Employee();
        employee.setId(22L);
        EmployeeAccount account = new EmployeeAccount();
        account.setId(11L);
        account.setEmployee(employee);
        account.setUsername("employee");
        account.setPasswordHash("encoded-password");
        return account;
    }
}
