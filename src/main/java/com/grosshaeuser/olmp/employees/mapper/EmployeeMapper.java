package com.grosshaeuser.olmp.employees.mapper;

import com.grosshaeuser.olmp.shared.MappingChecks;
import com.grosshaeuser.olmp.employees.dto.*;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Role;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class EmployeeMapper {
    public EmployeeDTO mapEmployeeToEmployeeDto(Employee employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getEmail(),
                employee.getLastName(),
                employee.getFirstName(),
                mapRolesToRoleNames(employee.getRoles()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(employee.getCreatedAt()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(employee.getUpdatedAt())
        );
    }

    public EmployeeEditDTO mergeEmployeeAndEmployeeAccountToEmployeeEditDto(Employee employee, EmployeeAccount account) {
        return new EmployeeEditDTO(
                employee.getId(),
                employee.getEmail(),
                employee.getLastName(),
                employee.getFirstName(),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(employee.getCreatedAt()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(employee.getUpdatedAt()),
                account.getId(),
                account.getUsername(),
                "",
                account.isEnabled(),
                account.getFailedLoginAttempts(),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getLockoutUntil()),
                account.isLocked(),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getCreatedAt()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getUpdatedAt()),
                MappingChecks.saveOffsetDateTimeToLocalDateTime(account.getLastAccess()),
                mapRolesToRoleNames(employee.getRoles())
        );
    }

    public Employee createEmployeeFromEmployeeRegistrationData(EmployeeRegistrationDTO dto, Set<Role> roles) {
        return Employee.builder()
                .email(dto.getEmail())
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .roles(roles)
                .build();
    }

    public EmployeeAccountCreateDTO createEmployeeAccountDTOFromEmployeeRegistrationData(EmployeeRegistrationDTO dto, Employee employee) {
        return new EmployeeAccountCreateDTO(
                employee,
                dto.getUsername(),
                dto.getPassword(),
                dto.isEnabled()
        );
    }

    public EmployeeAccountEditDTO createAccountEditDtoFromEmployeeUpdateData(EmployeeEditDTO dto) {
        return new EmployeeAccountEditDTO(
                dto.getAccountId(),
                dto.getUsername(),
                dto.getNewPassword(),
                dto.isEnabled(),
                dto.getFailedLoginAttempts(),
                dto.getLockoutUntil(),
                dto.isLocked()
        );
    }

    public HashSet<String> mapRolesToRoleNames(Set<Role> roles) {
        return new HashSet<>(roles.stream().map(Role::getName).toList());
    }

}
