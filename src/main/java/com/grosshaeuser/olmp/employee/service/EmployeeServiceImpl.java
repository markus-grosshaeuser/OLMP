package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.employee.repository.EmployeeRepository;
import com.grosshaeuser.olmp.employee.repository.EmployeeAccountRepository;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final AuthenticationContext authenticationContext;

    public Slice<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable) {
        Set<String> accessibleRoles = new HashSet<>();

        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_ADMIN_ACCOUNT)) {
            accessibleRoles.add(SecurityConstants.Roles.ADMIN);
        }
        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_STAFF_ACCOUNT)) {
            accessibleRoles.add(SecurityConstants.Roles.STAFF);
        }

        return employeeRepository.searchWithRolesIn(searchTerm, accessibleRoles, pageable).map(EmployeeMapper::mapEmplyeeToEmployeeDto);
    }
}
