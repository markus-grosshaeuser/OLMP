package com.grosshaeuser.olmp.employees.dto;

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
