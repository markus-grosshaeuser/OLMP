package com.grosshaeuser.olmp.members.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberDTO(
        Long id,
        String lastName,
        String firstName,
        LocalDate dateOfBirth,
        LocalDateTime creationTimestamp,
        LocalDateTime lastModifiedTimestamp
) {
}
