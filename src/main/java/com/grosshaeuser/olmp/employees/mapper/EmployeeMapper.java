package com.grosshaeuser.olmp.employees.mapper;

import com.grosshaeuser.olmp.employees.dto.*;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.entity.Role;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class EmployeeMapper {
    public static EmployeeDTO mapEmployeeToEmployeeDto(Employee employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getEmail(),
                employee.getLastName(),
                employee.getFirstName(),
                mapRolesToRoleNames(employee.getRoles()),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(employee.getCreatedAt()),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(employee.getUpdatedAt())
        );
    }

    public static EmployeeEditDTO mergeEmployeeAndEmployeeAccountToEmployeeEditDto(Employee employee, EmployeeAccount account) {
        return new EmployeeEditDTO(
                employee.getId(),
                employee.getEmail(),
                employee.getLastName(),
                employee.getFirstName(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(employee.getCreatedAt()),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(employee.getUpdatedAt()),
                account.getId(),
                account.getUsername(),
                "",
                account.isEnabled(),
                account.getFailedLoginAttempts(),
                account.getLockoutUntil() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(account.getLockoutUntil()) : null,
                account.isLocked(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(account.getCreatedAt()),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(account.getUpdatedAt()),
                account.getLastAccess() != null ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(account.getLastAccess()) : null,
                mapRolesToRoleNames(employee.getRoles())
        );
    }

    public static Employee createEmployeeFromEmployeeRegistrationData(EmployeeRegistrationDTO dto, Set<Role> roles) {
        return Employee.builder()
                .email(dto.getEmail())
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .roles(roles)
                .build();
    }

    public static EmployeeAccountCreateDTO createEmployeeAccountDTOFromEmployeeRegistrationData(EmployeeRegistrationDTO dto, Employee employee) {
        return new EmployeeAccountCreateDTO(
                employee,
                dto.getUsername(),
                dto.getPassword(),
                dto.isEnabled()
        );
    }

    public static EmployeeAccountEditDTO createAccountEditDtoFromEmployeeUpdateData(EmployeeEditDTO dto) {
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

    public static HashSet<String> mapRolesToRoleNames(Set<Role> roles) {
        return new HashSet<>(roles.stream().map(Role::getName).toList());
    }

}
