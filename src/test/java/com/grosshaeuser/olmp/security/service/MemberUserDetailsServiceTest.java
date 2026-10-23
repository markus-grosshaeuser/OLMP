package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
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
class MemberUserDetailsServiceTest {

    @Mock
    private MemberAccountRepository repository;
    @InjectMocks
    private MemberUserDetailsService service;

    @Test
    void findsMemberByEmailAndGrantsOnlyMemberRole() {
        when(repository.findByEmail("member@example.com")).thenReturn(Optional.of(account()));

        var principal = service.loadUserByUsername("member@example.com");

        assertThat(principal.getAccountId()).isEqualTo(11L);
        assertThat(principal.getMemberId()).isEqualTo(22L);
        assertThat(principal.getUsername()).isEqualTo("member@example.com");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_MEMBER");
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void unverifiedAndLockedAccountIsReturnedForAuthenticationProviderToReject() {
        MemberAccount account = account();
        account.setVerified(false);
        account.setLocked(true);
        when(repository.findByEmail("member@example.com")).thenReturn(Optional.of(account));

        var principal = service.loadUserByUsername("member@example.com");

        assertThat(principal.isEnabled()).isFalse();
        assertThat(principal.isAccountNonLocked()).isFalse();
    }

    @Test
    void missingMemberThrowsUsernameNotFound() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private MemberAccount account() {
        Member member = new Member();
        member.setId(22L);
        MemberAccount account = new MemberAccount();
        account.setId(11L);
        account.setMember(member);
        account.setEmail("member@example.com");
        account.setPasswordHash("encoded-password");
        account.setEnabled(true);
        account.setVerified(true);
        return account;
    }
}
