package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAccountServiceImpl implements MemberAccountService {

    private final MemberAccountRepository memberAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Optional<MemberAccount> createMemberAccount(MemberAccountCreateDTO dto) {
        MemberAccount account = MemberAccount.builder()
                .member(dto.getMember())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .verified(dto.isVerified())
                .enabled(dto.isEnabled())
                .build();
        return Optional.of(memberAccountRepository.save(account));
    }
}
