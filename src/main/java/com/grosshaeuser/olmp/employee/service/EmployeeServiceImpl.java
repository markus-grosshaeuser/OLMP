package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.entity.Employee;
import com.grosshaeuser.olmp.employee.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employee.entity.Role;
import com.grosshaeuser.olmp.employee.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.employee.repository.EmployeeRepository;
import com.grosshaeuser.olmp.employee.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.employee.repository.RoleRepository;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationContext authenticationContext;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable) {
        Set<String> accessibleRoles = new HashSet<>();

        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_ADMIN_ACCOUNT)) {
            accessibleRoles.add(SecurityConstants.Roles.ADMIN);
        }
        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_STAFF_ACCOUNT)) {
            accessibleRoles.add(SecurityConstants.Roles.STAFF);
        }

        return employeeRepository.searchWithRolesIn(searchTerm, accessibleRoles, pageable).map(EmployeeMapper::mapEmplyeeToEmployeeDto);
    }

    @Override
    @Transactional
    public Optional<EmployeeDTO> createEmployee(EmployeeCreateDTO dto) {
        validatePermissions(dto.getRoles());

        Set<Role> roles = new HashSet<>(roleRepository.findAllByNameIn(dto.getRoles()));

        if (roles.size() != dto.getRoles().size()) {
            Set<String> foundRoleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
            Set<String> missingRoles = new HashSet<>(dto.getRoles());
            missingRoles.removeAll(foundRoleNames);
            throw new IllegalArgumentException("The following roles were not found: " + String.join(", ", missingRoles));
        }

        Employee employee = Employee.builder()
                .email(dto.getEmail())
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .roles(roles)
                .build();
        Employee persistedEmployee = employeeRepository.save(employee);

        EmployeeAccount account = EmployeeAccount.builder()
                .employee(persistedEmployee)
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .enabled(dto.isEnabled())
                .build();

        employeeAccountRepository.save(account);

        return Optional.of(EmployeeMapper.mapEmplyeeToEmployeeDto(persistedEmployee));
    }

    private void validatePermissions(Set<String> requestedRoles) {
        if (requestedRoles.contains(SecurityConstants.Roles.ADMIN) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_ADMIN_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create an account with this role");
        }

        if (requestedRoles.contains(SecurityConstants.Roles.STAFF) && !authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_STAFF_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create an account with this role");
        }
    }
}
