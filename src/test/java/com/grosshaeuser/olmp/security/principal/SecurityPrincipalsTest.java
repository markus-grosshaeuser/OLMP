package com.grosshaeuser.olmp.security.principal;

import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPrincipalsTest {

    @Test
    void employeePrincipalExposesAccountIdentityAndAuthorities() {
        EmployeeAccount account = employeeAccount();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN"),
                new SimpleGrantedAuthority("MEMBER_READ"));

        EmployeePrincipal principal = new EmployeePrincipal(account, authorities);

        assertThat(principal.getAccountId()).isEqualTo(11L);
        assertThat(principal.getEmployeeId()).isEqualTo(22L);
        assertThat(principal.getUsername()).isEqualTo("employee");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_LIBRARIAN", "MEMBER_READ");
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void memberPrincipalExposesAccountIdentityAndAuthorities() {
        MemberAccount account = memberAccount();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));

        MemberPrincipal principal = new MemberPrincipal(account, authorities);

        assertThat(principal.getAccountId()).isEqualTo(33L);
        assertThat(principal.getMemberId()).isEqualTo(44L);
        assertThat(principal.getUsername()).isEqualTo("member@example.com");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_MEMBER");
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "employee, false, none, true", "employee, false, past, true",
            "employee, false, future, false", "employee, true, none, false",
            "employee, true, past, false", "employee, true, future, false",
            "member, false, none, true", "member, false, past, true",
            "member, false, future, false", "member, true, none, false",
            "member, true, past, false", "member, true, future, false"
    })
    void lockFlagAndLockoutTimeDetermineWhetherAccountIsNonLocked(
            String type, boolean locked, String lockout, boolean expected) {
        OffsetDateTime until = switch (lockout) {
            case "past" -> OffsetDateTime.now().minusMinutes(1);
            case "future" -> OffsetDateTime.now().plusMinutes(1);
            default -> null;
        };
        UserDetails principal;
        if (type.equals("employee")) {
            EmployeeAccount account = employeeAccount();
            account.setLocked(locked);
            account.setLockoutUntil(until);
            principal = new EmployeePrincipal(account, List.of());
        } else {
            MemberAccount account = memberAccount();
            account.setLocked(locked);
            account.setLockoutUntil(until);
            principal = new MemberPrincipal(account, List.of());
        }

        assertThat(principal.isAccountNonLocked()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void employeeEnabledStatusComesFromAccount(boolean enabled) {
        EmployeeAccount account = employeeAccount();
        account.setEnabled(enabled);

        assertThat(new EmployeePrincipal(account, List.of()).isEnabled()).isEqualTo(enabled);
    }

    @ParameterizedTest
    @CsvSource({"false, false, false", "false, true, false", "true, false, false", "true, true, true"})
    void memberMustBeEnabledAndVerified(boolean enabled, boolean verified, boolean expected) {
        MemberAccount account = memberAccount();
        account.setEnabled(enabled);
        account.setVerified(verified);

        MemberPrincipal principal = new MemberPrincipal(account, List.of());

        assertThat(principal.isVerified()).isEqualTo(verified);
        assertThat(principal.isEnabled()).isEqualTo(expected);
    }

    private EmployeeAccount employeeAccount() {
        Employee employee = new Employee();
        employee.setId(22L);
        EmployeeAccount account = new EmployeeAccount();
        account.setId(11L);
        account.setEmployee(employee);
        account.setUsername("employee");
        account.setPasswordHash("encoded-password");
        return account;
    }

    private MemberAccount memberAccount() {
        Member member = new Member();
        member.setId(44L);
        MemberAccount account = new MemberAccount();
        account.setId(33L);
        account.setMember(member);
        account.setEmail("member@example.com");
        account.setPasswordHash("encoded-password");
        return account;
    }
}
