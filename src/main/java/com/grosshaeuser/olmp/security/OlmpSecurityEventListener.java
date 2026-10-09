package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import com.grosshaeuser.olmp.employee.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class OlmpSecurityEventListener {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final EmployeeAccountRepository employeeAccountRepository;
    private final MemberAccountRepository memberAccountRepository;

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof EmployeePrincipal employeePrincipal) {
            employeeAccountRepository.findById(employeePrincipal.getAccountId()).ifPresent(account -> {
                account.setFailedLoginAttempts(0);
                account.setLockoutUntil(null);
                account.setLocked(false);
                account.setLastAccess(OffsetDateTime.now());
            });
        }

        if (principal instanceof MemberPrincipal memberPrincipal) {
            memberAccountRepository.findById(memberPrincipal.getAccountId()).ifPresent(account -> {
                account.setFailedLoginAttempts(0);
                account.setLockoutUntil(null);
                account.setLocked(false);
                account.setLastAccess(OffsetDateTime.now());
            });
        }
    }

    @EventListener
    @Transactional
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();

        employeeAccountRepository.findByUsername(username).ifPresent(account -> {
            int failedAttempts = account.getFailedLoginAttempts() + 1;
            account.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                account.setLocked(true);
                account.setLockoutUntil(OffsetDateTime.now().plusMinutes(15));
            }
        });

        memberAccountRepository.findByEmail(username).ifPresent(account -> {
            int failedAttempts = account.getFailedLoginAttempts() + 1;
            account.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                account.setLocked(true);
                account.setLockoutUntil(OffsetDateTime.now().plusMinutes(15));
            }
        });
    }
}