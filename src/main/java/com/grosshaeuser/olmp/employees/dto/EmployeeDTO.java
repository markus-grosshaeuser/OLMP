package com.grosshaeuser.olmp.employees.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record EmployeeDTO(
        Long id,
        String email,
        String lastName,
        String firstName,
        Set<String> roles,
        LocalDateTime creationTimestamp,
        LocalDateTime lastModifiedTimestamp
        ) {
}
