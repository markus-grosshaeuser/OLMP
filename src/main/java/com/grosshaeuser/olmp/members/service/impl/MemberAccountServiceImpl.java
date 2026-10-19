package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAccountServiceImpl implements MemberAccountService {

    private final MemberAccountRepository memberAccountRepository;
    private final PasswordProperties passwordProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public Optional<MemberAccount> createMemberAccount(MemberAccountCreateDTO dto) {
        checkPermission(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT, "You have no permission to create a member account.");
        validateEmailIsAvailable(dto.getEmail());
        validatePassword(dto.getPassword());
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
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read a member account.");
        return memberAccountRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<MemberAccount> update(MemberAccountEditDTO memberAccountEditDTO) {
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBER_ACCOUNT, "You have no permission to update a member account.");
        if (memberAccountEditDTO.isVerified()) {
            checkPermission(SecurityConstants.Privileges.VERIFY_MEMBER, "You have no permission to verify a member.");
        }
        return memberAccountRepository.findById(memberAccountEditDTO.getId())
                .map(account -> {
                    validateEmailIsAvailableForUpdate(account, memberAccountEditDTO.getEmail());
                    account.setEmail(memberAccountEditDTO.getEmail());
                    if (memberAccountEditDTO.getNewPassword() != null && !memberAccountEditDTO.getNewPassword().isBlank()) {
                        account.setPasswordHash(passwordEncoder.encode(memberAccountEditDTO.getNewPassword()));
                    }
                    account.setVerified(memberAccountEditDTO.isVerified());
                    account.setEnabled(memberAccountEditDTO.isEnabled());
                    account.setLocked(memberAccountEditDTO.isLocked());
                    account.setFailedLoginAttempts(memberAccountEditDTO.getFailedLoginAttempts());
                    account.setLockoutUntil(MemberMapper.saveLocalDateTimeToOffsetDateTime(memberAccountEditDTO.getLockoutUntil()));
                    return memberAccountRepository.save(account);
                });
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    private void validateEmailIsAvailable(String requestedEmail) {
        if (memberAccountRepository.existsByEmail((requestedEmail))) {
            throw new DataIntegrityViolationException("Email already exists: " + requestedEmail);
        }
    }

    private void validateEmailIsAvailableForUpdate(MemberAccount existingAccount, String requestedEmail) {
        if (!existingAccount.getEmail().equals(requestedEmail) && memberAccountRepository.existsByEmail((requestedEmail))) {
            throw new DataIntegrityViolationException("Email already exists: " + requestedEmail);
        }
    }

    private void validatePassword(String password) {
        if (!password.matches(passwordProperties.getRegExPattern())) {
            throw new IllegalArgumentException(passwordProperties.getPasswordRequirementDescription());
        }
    }
}
