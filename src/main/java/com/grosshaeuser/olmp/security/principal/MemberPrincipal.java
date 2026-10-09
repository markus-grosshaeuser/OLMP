package com.grosshaeuser.olmp.security.principal;

import com.grosshaeuser.olmp.members.entity.MemberAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;

@Getter
public class MemberPrincipal implements UserDetails {

    private final Long accountId;
    private final Long memberId;
    private final String username;
    private final String password;
    private final boolean verified;
    private final boolean enabled;
    private final boolean locked;
    private final OffsetDateTime lockoutUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    public MemberPrincipal(
            MemberAccount account,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.accountId = account.getId();
        this.memberId = account.getMember().getId();
        this.username = account.getEmail();
        this.password = account.getPasswordHash();
        this.verified = account.isVerified();
        this.enabled = account.isEnabled();
        this.locked = account.isLocked();
        this.lockoutUntil = account.getLockoutUntil();
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked && (lockoutUntil == null || lockoutUntil.isBefore(OffsetDateTime.now()));
    }

    @Override
    public boolean isEnabled() {
        return enabled && verified;
    }
}