package com.grosshaeuser.olmp.employee.mapper;

import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.entity.Employee;
import com.grosshaeuser.olmp.employee.entity.Role;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class EmployeeMapper {
    public static EmployeeDTO mapEmplyeeToEmployeeDto(Employee employee) {
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


    private static HashSet<String> mapRolesToRoleNames(Set<Role> roles) {
        HashSet<String> roleNames = new HashSet<>();
        for (Role role : roles) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }

}
