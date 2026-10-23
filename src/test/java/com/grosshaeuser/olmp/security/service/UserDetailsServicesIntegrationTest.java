package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Privilege;
import com.grosshaeuser.olmp.employees.entity.Role;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserDetailsServicesIntegrationTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EmployeeUserDetailsService employeeService;
    @Autowired
    private MemberUserDetailsService memberService;

    @Test
    void employeeLookupLoadsPersistedRolesAndLazyPrivileges() {
        Privilege privilege = new Privilege();
        privilege.setName("SECURITY_TEST_READ");
        entityManager.persist(privilege);
        Role role = new Role();
        role.setName("SECURITY_TEST_ROLE");
        role.addPrivilege(privilege);
        entityManager.persist(role);
        Employee employee = new Employee();
        employee.setFirstName("Security");
        employee.setLastName("Test");
        employee.addRole(role);
        entityManager.persist(employee);
        EmployeeAccount account = new EmployeeAccount();
        account.setEmployee(employee);
        account.setUsername("security-details-employee");
        account.setPasswordHash("encoded-employee-password");
        account.setEnabled(false);
        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        var principal = employeeService.loadUserByUsername("security-details-employee");

        assertThat(principal.getAccountId()).isEqualTo(account.getId());
        assertThat(principal.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(principal.getPassword()).isEqualTo("encoded-employee-password");
        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_SECURITY_TEST_ROLE", "SECURITY_TEST_READ");
    }

    @Test
    void memberLookupLoadsPersistedIdentityAndVerificationStatus() {
        Member member = new Member();
        member.setFirstName("Security");
        member.setLastName("Test");
        entityManager.persist(member);
        MemberAccount account = new MemberAccount();
        account.setMember(member);
        account.setEmail("security-details-member@example.com");
        account.setPasswordHash("encoded-member-password");
        account.setEnabled(true);
        account.setVerified(false);
        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        var principal = memberService.loadUserByUsername("security-details-member@example.com");

        assertThat(principal.getAccountId()).isEqualTo(account.getId());
        assertThat(principal.getMemberId()).isEqualTo(member.getId());
        assertThat(principal.getUsername()).isEqualTo("security-details-member@example.com");
        assertThat(principal.getPassword()).isEqualTo("encoded-member-password");
        assertThat(principal.isVerified()).isFalse();
        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_MEMBER");
    }
}
