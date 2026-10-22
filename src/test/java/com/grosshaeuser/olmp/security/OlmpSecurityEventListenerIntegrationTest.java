package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OlmpSecurityEventListenerIntegrationTest {

    private static final String PASSWORD = "security-test-password";
    private static final OffsetDateTime PREVIOUS_ACCESS = OffsetDateTime.parse("2026-01-01T12:00:00Z");

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long employeeAccountId;
    private Long memberAccountId;

    @BeforeEach
    void createAccounts() {
        String passwordHash = passwordEncoder.encode(PASSWORD);
        Employee employee = new Employee();
        employee.setFirstName("Security");
        employee.setLastName("Test");
        entityManager.persist(employee);
        EmployeeAccount employeeAccount = new EmployeeAccount();
        employeeAccount.setEmployee(employee);
        employeeAccount.setUsername("event-test-employee");
        employeeAccount.setPasswordHash(passwordHash);
        employeeAccount.setEnabled(true);
        employeeAccount.setLastAccess(PREVIOUS_ACCESS);
        entityManager.persist(employeeAccount);
        employeeAccountId = employeeAccount.getId();

        Member member = new Member();
        member.setFirstName("Security");
        member.setLastName("Test");
        entityManager.persist(member);
        MemberAccount memberAccount = new MemberAccount();
        memberAccount.setMember(member);
        memberAccount.setEmail("event-test-member@example.com");
        memberAccount.setPasswordHash(passwordHash);
        memberAccount.setEnabled(true);
        memberAccount.setVerified(true);
        memberAccount.setLastAccess(PREVIOUS_ACCESS);
        entityManager.persist(memberAccount);
        memberAccountId = memberAccount.getId();
        flushAndClear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "member"})
    void authenticationSuccessPublishesEventAndPersistsReset(String accountType) {
        OffsetDateTime expiredLockout = OffsetDateTime.now().minusMinutes(1);
        if (accountType.equals("employee")) {
            EmployeeAccount account = entityManager.find(EmployeeAccount.class, employeeAccountId);
            account.setFailedLoginAttempts(4);
            account.setLockoutUntil(expiredLockout);
        } else {
            MemberAccount account = entityManager.find(MemberAccount.class, memberAccountId);
            account.setFailedLoginAttempts(4);
            account.setLockoutUntil(expiredLockout);
        }
        flushAndClear();
        OffsetDateTime before = OffsetDateTime.now();

        authenticationManager(accountType).authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username(accountType), PASSWORD));
        flushAndClear();

        EmployeeAccount employee = entityManager.find(EmployeeAccount.class, employeeAccountId);
        MemberAccount member = entityManager.find(MemberAccount.class, memberAccountId);
        assertThat(employee.getFailedLoginAttempts()).isZero();
        assertThat(member.getFailedLoginAttempts()).isZero();
        assertThat(employee.isLocked()).isFalse();
        assertThat(member.isLocked()).isFalse();
        assertThat(employee.getLockoutUntil()).isNull();
        assertThat(member.getLockoutUntil()).isNull();
        if (accountType.equals("employee")) {
            assertThat(employee.getLastAccess()).isBetween(before, OffsetDateTime.now());
            assertThat(member.getLastAccess()).isEqualTo(PREVIOUS_ACCESS);
        } else {
            assertThat(member.getLastAccess()).isBetween(before, OffsetDateTime.now());
            assertThat(employee.getLastAccess()).isEqualTo(PREVIOUS_ACCESS);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "member"})
    void badCredentialsPublishEventsAndPersistLockoutAfterFiveFailures(String accountType) {
        AuthenticationManager manager = authenticationManager(accountType);
        OffsetDateTime before = OffsetDateTime.now();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> manager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username(accountType), "wrong-password")))
                    .isInstanceOf(BadCredentialsException.class);
        }
        flushAndClear();

        EmployeeAccount employee = entityManager.find(EmployeeAccount.class, employeeAccountId);
        MemberAccount member = entityManager.find(MemberAccount.class, memberAccountId);
        if (accountType.equals("employee")) {
            assertThat(employee.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(employee.isLocked()).isTrue();
            assertThat(employee.getLockoutUntil()).isBetween(before.plusMinutes(15), OffsetDateTime.now().plusMinutes(15));
            assertThat(member.getFailedLoginAttempts()).isZero();
            assertThat(member.isLocked()).isFalse();
            assertThat(member.getLockoutUntil()).isNull();
        } else {
            assertThat(member.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(member.isLocked()).isTrue();
            assertThat(member.getLockoutUntil()).isBetween(before.plusMinutes(15), OffsetDateTime.now().plusMinutes(15));
            assertThat(employee.getFailedLoginAttempts()).isZero();
            assertThat(employee.isLocked()).isFalse();
            assertThat(employee.getLockoutUntil()).isNull();
        }
        assertThat(employee.getLastAccess()).isEqualTo(PREVIOUS_ACCESS);
        assertThat(member.getLastAccess()).isEqualTo(PREVIOUS_ACCESS);

        // Lockout must be enforced on the next login even with the correct password.
        assertThatThrownBy(() -> manager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username(accountType), PASSWORD)))
                .isInstanceOf(LockedException.class);
    }

    private AuthenticationManager authenticationManager(String accountType) {
        return applicationContext.getBean(accountType + "AuthenticationManager", AuthenticationManager.class);
    }

    private String username(String accountType) {
        return accountType.equals("employee") ? "event-test-employee" : "event-test-member@example.com";
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
