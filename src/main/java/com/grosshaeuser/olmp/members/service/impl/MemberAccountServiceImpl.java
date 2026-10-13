package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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

    @Override
    public Optional<MemberAccount> findByMemberId(Long memberId) {
        return memberAccountRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<MemberAccount> update(MemberAccountEditDTO memberAccountEditDTO) {
        return memberAccountRepository.findById(memberAccountEditDTO.getId())
                .map(account -> {
                    account.setEmail(memberAccountEditDTO.getEmail());
                    account.setPasswordHash(passwordEncoder.encode(memberAccountEditDTO.getNewPassword()));
                    account.setVerified(memberAccountEditDTO.isVerified());
                    account.setEnabled(memberAccountEditDTO.isEnabled());
                    account.setLocked(memberAccountEditDTO.isLocked());
                    account.setFailedLoginAttempts(memberAccountEditDTO.getFailedLoginAttempts());
                    account.setLockoutUntil(parseDateTimeString(memberAccountEditDTO.getLockoutUntil()));
                    return memberAccountRepository.save(account);
                });
    }

    private OffsetDateTime parseDateTimeString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateTimeString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
