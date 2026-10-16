package com.grosshaeuser.olmp.employees.service.impl;

import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employees.service.EmployeeAccountService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeAccountServiceImpl implements EmployeeAccountService {

    private final EmployeeAccountRepository employeeAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordProperties passwordProperties;


    @Override
    public Optional<EmployeeAccount> findById(Long employeeId) {
        return employeeAccountRepository.findById(employeeId);
    }

    @Override
    public Optional<EmployeeAccount> createEmployeeAccount(EmployeeAccountCreateDTO dto) {
        validatePassword(dto.getPassword());
        validateUsernameIsAvailable(dto.getUsername());
        employeeAccountRepository.save(EmployeeAccount.builder()
                .employee(dto.getEmployee())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .enabled(dto.isEnabled())
                .build());
        return Optional.empty();
    }

    @Override
    public Optional<EmployeeAccount> updateEmployeeAccount(EmployeeAccountEditDTO dto) {
        EmployeeAccount existingAccount = employeeAccountRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + dto.getId()));
        validateUsernameIsAvailableForUpdate(existingAccount, dto.getUsername());

        existingAccount.setUsername(dto.getUsername());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            validatePassword(dto.getNewPassword());
            existingAccount.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        existingAccount.setEnabled(dto.isEnabled());
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
}
