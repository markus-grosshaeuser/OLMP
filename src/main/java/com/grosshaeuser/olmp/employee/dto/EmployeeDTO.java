package com.grosshaeuser.olmp.employee.dto;

import java.util.Set;

public record EmployeeDTO(
        Long id,
        String email,
        String lastName,
        String firstName,
        Set<String> roles,
        String creationTimestamp,
        String lastModifiedTimestamp
        ) {
}
