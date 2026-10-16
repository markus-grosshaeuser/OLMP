package com.grosshaeuser.olmp.employees.service.impl;

import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employees.service.EmployeeAccountService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;

@Service
@RequiredArgsConstructor
class EmployeeAccountServiceImpl implements EmployeeAccountService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordProperties passwordProperties;
    private final AuthenticationContext authenticationContext;


    @Override
    public Optional<EmployeeAccount> findById(Long employeeId) {
        return employeeAccountRepository.findById(employeeId);
    }

    @Override
    public Optional<EmployeeAccount> createEmployeeAccount(EmployeeAccountCreateDTO dto) {
        validatePassword(dto.getPassword());
        validateUsernameIsAvailable(dto.getUsername());
        EmployeeAccount account = employeeAccountRepository.save(EmployeeAccount.builder()
                .employee(dto.getEmployee())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .enabled(dto.isEnabled())
                .build());
        return Optional.of(account);
    }

    @Override
    public Optional<EmployeeAccount> updateEmployeeAccount(EmployeeAccountEditDTO dto) {
        EmployeeAccount existingAccount = employeeAccountRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + dto.getId()));
        validateUsernameIsAvailableForUpdate(existingAccount, dto.getUsername());

        existingAccount.setUsername(dto.getUsername());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            checkPermission(RESET_PASSWORD, "You have no permission to change passwords");
            validatePassword(dto.getNewPassword());
            existingAccount.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        validateEnabledChange(existingAccount, dto);
        existingAccount.setEnabled(dto.isEnabled());
        validateLockChange(existingAccount, dto);
        existingAccount.setLocked(dto.isLocked());

        return Optional.of(employeeAccountRepository.save(existingAccount));
    }

    @Override
    public Optional<EmployeeAccount> findByUsername(String username) {
        return employeeAccountRepository.findByUsername(username);
    }

    private void validateUsernameIsAvailable(String username) {
        if (employeeAccountRepository.existsByUsername(username)) {
            throw new DataIntegrityViolationException("Username already exists: " + username);
        }
    }

    private void validateUsernameIsAvailableForUpdate(EmployeeAccount existingAccount, String requestedUsername) {
        if (!existingAccount.getUsername().equals(requestedUsername) && employeeAccountRepository.existsByUsername(requestedUsername)) {
            throw new DataIntegrityViolationException("Username already exists: " + requestedUsername);
        }
    }

    private void validatePassword(String password) {
        if (!password.matches(passwordProperties.getRegExPattern())) {
            throw new IllegalArgumentException(passwordProperties.getPasswordRequirementDescription());
        }
    }

    private void validateEnabledChange(EmployeeAccount account, EmployeeAccountEditDTO dto) {
        if (!account.isEnabled() && dto.isEnabled()) {
            checkPermission(ENABLE_USER, "You have no permission to enable users");
        }

        if (account.isEnabled() && !dto.isEnabled()) {
            checkPermission(DISABLE_USER, "You have no permission to disable users");
        }
    }

    private void validateLockChange(EmployeeAccount account, EmployeeAccountEditDTO dto) {
        if (!account.isLocked() && dto.isLocked()) {
            checkPermission(LOCK_USER, "You have no permission to lock users");
        }

        if (account.isLocked() && !dto.isLocked()) {
            checkPermission(UNLOCK_USER, "You have no permission to unlock users");
        }
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
