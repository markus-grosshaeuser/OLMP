package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberAccountRepository memberAccountRepository;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public MemberPrincipal loadUserByUsername(@NonNull String email) {
        MemberAccount account = memberAccountRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Member account not found"
                ));

        return new MemberPrincipal(
                account,
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }
}
