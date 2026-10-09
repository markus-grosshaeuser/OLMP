package com.grosshaeuser.olmp.security.principal;

import com.grosshaeuser.olmp.security.entity.EmployeeAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;

@Getter
public class EmployeePrincipal implements UserDetails {

    private final Long accountId;
    private final Long employeeId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final boolean locked;
    private final OffsetDateTime lockoutUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    public EmployeePrincipal(
            EmployeeAccount account,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.accountId = account.getId();
        this.employeeId = account.getEmployee().getId();
        this.username = account.getUsername();
        this.password = account.getPasswordHash();
        this.enabled = account.isEnabled();
        this.locked = account.isLocked();
        this.lockoutUntil = account.getLockoutUntil();
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked && (lockoutUntil == null || lockoutUntil.isBefore(OffsetDateTime.now()));
    }
}