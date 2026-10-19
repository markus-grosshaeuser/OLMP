package com.grosshaeuser.olmp.employees.service.impl;

import com.grosshaeuser.olmp.employees.dto.*;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Role;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.employees.repository.EmployeeRepository;
import com.grosshaeuser.olmp.employees.repository.RoleRepository;
import com.grosshaeuser.olmp.employees.service.EmployeeAccountService;
import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccountService employeeAccountService;
    private final RoleRepository roleRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable) {
        Set<String> accessibleRoles = getReadableRoles();
        return employeeRepository.searchWithRolesIn(searchTerm, accessibleRoles, pageable).map(EmployeeMapper::mapEmployeeToEmployeeDto);
    }

    @Override
    @Transactional
    public Optional<EmployeeDTO> createEmployee(EmployeeRegistrationDTO dto) {
        validateCreatePermission(dto.getRoles());
        validateAssignableRoles(dto.getRoles());
        validateEmailIsAvailable(dto.getEmail());

        Set<Role> roles = new HashSet<>(roleRepository.findAllByNameIn(dto.getRoles()));

        if (roles.size() != dto.getRoles().size()) {
            throw new IllegalArgumentException("Attempted to create an employee with non-existent roles");
        }

        Employee persistedEmployee = employeeRepository.save(EmployeeMapper.createEmployeeFromEmployeeRegistrationData(dto, roles));
        employeeAccountService.createEmployeeAccount(EmployeeMapper.createEmployeeAccountDTOFromEmployeeRegistrationData(dto, persistedEmployee));

        return Optional.of(EmployeeMapper.mapEmployeeToEmployeeDto(persistedEmployee));
    }

    @Override
    public Optional<EmployeeEditDTO> getDetailedEmployeeById(Long id) {
        return employeeRepository.findById(id).flatMap(employee -> {
            Set<String> readableRoles = getReadableRoles();
            validateRolePermission(readableRoles, employee.getRoles());
            return employeeAccountService.findById(employee.getId())
                    .map(account ->
                            EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(employee, account)
                    );
        });
    }

    @Transactional
    @Override
    public Optional<EmployeeEditDTO> updateEmployee(EmployeeEditDTO dto) {
        validateAssignableRoles(dto.getRoles());

        Set<Role> roles = new HashSet<>(roleRepository.findAllByNameIn(dto.getRoles()));
        if (roles.size() != dto.getRoles().size()) {
            throw new IllegalArgumentException("Attempted to create an employee with non-existent roles");
        }

        Employee existingEmployee = employeeRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + dto.getId()));

        validateUpdatePermission(existingEmployee.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        validateEmailIsAvailableForUpdate(existingEmployee, dto.getEmail());

        existingEmployee.setFirstName(dto.getFirstName());
        existingEmployee.setLastName(dto.getLastName());
        existingEmployee.setEmail(dto.getEmail());
        existingEmployee.replaceRoles(roles);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        EmployeeAccount updatedAccount = employeeAccountService.updateEmployeeAccount(EmployeeMapper.createAccountEditDtoFromEmployeeUpdateData(dto))
                .orElseThrow(() -> new RuntimeException("Account Update failed."));

        return Optional.of(EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(updatedEmployee, updatedAccount));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        validateDeletePermission(employee.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        employeeRepository.delete(employee);
    }

    @Override
    public Optional<EmployeeEditDTO> getCurrentEmployee() {
        EmployeeAccount currentAccount = getCurrentEmployeeAccount()
                .orElseThrow(() -> new AccessDeniedException("No authenticated employee account found."));
        Employee currentEmployee = employeeRepository.findById(currentAccount.getEmployee().getId())
                .orElseThrow(() -> new AccessDeniedException("No authenticated employee found."));

        return Optional.of(EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(currentEmployee, currentAccount));
    }

    @Transactional
    @Override
    public Optional<EmployeeEditDTO> updateCurrentEmployee(EmployeeEditDTO dto) {
        EmployeeAccount currentAccount = getCurrentEmployeeAccount()
                .orElseThrow(() -> new AccessDeniedException("No authenticated employee account found."));

        Employee currentEmployee = currentAccount.getEmployee();

        if (!currentEmployee.getId().equals(dto.getId())) {
            throw new AccessDeniedException("You may only update your own employee data.");
        }

        if (!currentAccount.getId().equals(dto.getAccountId())) {
            throw new AccessDeniedException("You may only update your own employee account.");
        }

        validateEmailIsAvailableForUpdate(currentEmployee, dto.getEmail());

        currentEmployee.setFirstName(dto.getFirstName());
        currentEmployee.setLastName(dto.getLastName());
        currentEmployee.setEmail(dto.getEmail());

        Employee updatedEmployee = employeeRepository.save(currentEmployee);

        EmployeeEditDTO sanitizedDto = dto.toBuilder()
                .roles(EmployeeMapper.mapRolesToRoleNames(currentEmployee.getRoles()))
                .enabled(currentAccount.isEnabled())
                .failedLoginAttempts(currentAccount.getFailedLoginAttempts())
                .lockoutUntil(currentAccount.getLockoutUntil() != null ? currentAccount.getLockoutUntil().toLocalDateTime() : null)
                .locked(currentAccount.isLocked())
                .build();

        EmployeeAccount updatedAccount = employeeAccountService.updateEmployeeAccount(
                EmployeeMapper.createAccountEditDtoFromEmployeeUpdateData(sanitizedDto)
        ).orElseThrow(() -> new RuntimeException("Account update failed."));

        return Optional.of(EmployeeMapper.mergeEmployeeAndEmployeeAccountToEmployeeEditDto(
                updatedEmployee,
                updatedAccount
        ));
    }

    @Override
    public long countEmployees(String query) {
        return employeeRepository.countSearch(query, getReadableRoles());
    }

    private Optional<EmployeeAccount> getCurrentEmployeeAccount() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> employeeAccountService.findByUsername(userDetails.getUsername()));
    }

    private void validateRolePermission(Set<String> readableRoles, Set<Role> roles) {
        if (!readableRoles.containsAll(roles.stream().map(Role::getName).collect(Collectors.toSet()))) {
            throw new AccessDeniedException("You have no permission to manage this account");
        }
    }

    private Set<String> getReadableRoles() {
        Set<String> readableRoles = new HashSet<>();

        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_ADMIN_ACCOUNT)) {
            readableRoles.add(SecurityConstants.Roles.ADMIN);
        }
        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_STAFF_ACCOUNT)) {
            readableRoles.add(SecurityConstants.Roles.STAFF);
        }
        if (readableRoles.isEmpty()) {
            throw new AccessDeniedException("You have no permission to read employee accounts");
        }
        return readableRoles;
    }

    private void validateAssignableRoles(Set<String> requestedRoles) {
        if (requestedRoles.contains(SecurityConstants.Roles.ADMIN) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.ASSIGN_ADMIN_ROLE)) {
            throw new AccessDeniedException("You have no permission to manage an administrative account");
        }

        if (requestedRoles.contains(SecurityConstants.Roles.STAFF) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.ASSIGN_STAFF_ROLE)) {
            throw new AccessDeniedException("You have no permission to manage a staff account");
        }
    }

    private void validateCreatePermission(Set<String> requestedRoles) {
        if (requestedRoles.contains(SecurityConstants.Roles.ADMIN) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_ADMIN_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create an administrative account");
        }

        if (requestedRoles.contains(SecurityConstants.Roles.STAFF) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_STAFF_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create a staff account");
        }
    }

    private void validateUpdatePermission(Set<String> requestedRoles) {
        if (requestedRoles.contains(SecurityConstants.Roles.ADMIN) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.UPDATE_ADMIN_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to update an administrative account");
        }

        if (requestedRoles.contains(SecurityConstants.Roles.STAFF) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.UPDATE_STAFF_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to update a staff account");
        }
    }

    private void validateDeletePermission(Set<String> requestedRoles) {
        if (requestedRoles.contains(SecurityConstants.Roles.ADMIN) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.DELETE_ADMIN_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to delete an administrative account");
        }

        if (requestedRoles.contains(SecurityConstants.Roles.STAFF) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.DELETE_STAFF_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to delete a staff account");
        }
    }

    private void validateEmailIsAvailable(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email already exists: " + email);
        }
    }

    private void validateEmailIsAvailableForUpdate(Employee existingUser, String requestedEmail) {
        if (!Objects.equals(existingUser.getEmail(), requestedEmail) && employeeRepository.existsByEmail(requestedEmail)) {
            throw new DataIntegrityViolationException("Email already exists: " + requestedEmail);
        }
    }
}
