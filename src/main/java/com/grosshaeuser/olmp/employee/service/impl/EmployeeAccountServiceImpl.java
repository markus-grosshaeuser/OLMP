package com.grosshaeuser.olmp.employee.service.impl;

import com.grosshaeuser.olmp.employee.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employee.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employee.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employee.service.EmployeeAccountService;
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

    @Override
    public Optional<EmployeeAccount> findById(Long employeeId) {
        return employeeAccountRepository.findById(employeeId);
    }

    @Override
    public Optional<EmployeeAccount> createEmployeeAccount(EmployeeAccountCreateDTO dto) {
        validateUsernameIsAvailable(dto.getUsername());
        employeeAccountRepository.save(EmployeeAccount.builder()
                .employee(dto.getEmployee())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build());
        return Optional.empty();
    }

    @Override
    public Optional<EmployeeAccount> updateEmployeeAccount(EmployeeAccountEditDTO dto) {
        EmployeeAccount existingAccount = employeeAccountRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + dto.getId()));
        validateUsernameIsAvailableForUpdate(existingAccount, dto.getUsername());

        existingAccount.setUsername(dto.getUsername());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            existingAccount.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        existingAccount.setEnabled(dto.isEnabled());

        return Optional.of(employeeAccountRepository.save(existingAccount));
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
}
